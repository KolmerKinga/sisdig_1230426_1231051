package pt.isep.aisafe.airport.shared.forwarding;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.aisafe.airport.shared.config.AisafeProperties;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Peer-to-peer communication between replicas of THIS service.
 *
 * <ul>
 *   <li>Every call carries the user's JWT, the service key, the correlation id and
 *       {@code X-Forwarded-By}; a peer that receives {@code X-Forwarded-By} answers from its
 *       local store only, so a request is forwarded at most once (no circular forwarding).</li>
 *   <li>Connect timeout 1 s, read timeout 2 s, 2 attempts with exponential backoff.</li>
 *   <li>Circuit breaker: a peer that fails is skipped for 10 s (fail fast).</li>
 * </ul>
 */
@Component
public class PeerClient {

    private static final Logger log = LoggerFactory.getLogger(PeerClient.class);

    private final RestClient restClient;
    private final AisafeProperties properties;
    private final ExecutorService executor;
    private final ObjectMapper objectMapper;
    private final Map<String, Instant> peerDownUntil = new ConcurrentHashMap<>();

    public PeerClient(RestClient peerRestClient, AisafeProperties properties,
                      ExecutorService peerExecutor, ObjectMapper objectMapper) {
        this.restClient = peerRestClient;
        this.properties = properties;
        this.executor = peerExecutor;
        this.objectMapper = objectMapper;
    }

    public List<String> peers() {
        return properties.peers();
    }

    /**
     * Sends the request to the peers one by one and returns the first answer that is not 404.
     * Used for single-resource GETs and for commands (PATCH/POST) on a resource held by a peer.
     */
    public PeerLookup forwardToPeers(HttpMethod method, String pathAndQuery, Object body) {
        Map<String, String> headers = outgoingHeaders();
        boolean partial = false;
        for (String peer : properties.peers()) {
            if (isMarkedDown(peer)) {
                log.warn("Skipping peer {} (circuit open)", peer);
                partial = true;
                continue;
            }
            Optional<PeerResponse> answer = callWithRetry(peer, method, pathAndQuery, body, headers);
            if (answer.isEmpty()) {
                partial = true;
                continue;
            }
            if (answer.get().isNotFound()) {
                log.info("Peer {} does not have {} {}", peer, method, pathAndQuery);
                continue;
            }
            log.info("Peer {} answered {} for {} {}", peer, answer.get().status(), method, pathAndQuery);
            return new PeerLookup(answer, partial);
        }
        return new PeerLookup(Optional.empty(), partial);
    }

    /**
     * Sends the same GET to every peer in parallel and collects the returned lists.
     * Used to aggregate collection queries (search, grouping).
     */
    public <T> PeerCollection<T> collectFromPeers(String pathAndQuery, Class<T> elementType) {
        Map<String, String> headers = outgoingHeaders();
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);

        List<CompletableFuture<Optional<List<T>>>> futures = new ArrayList<>();
        for (String peer : properties.peers()) {
            if (isMarkedDown(peer)) {
                log.warn("Skipping peer {} (circuit open)", peer);
                futures.add(CompletableFuture.completedFuture(Optional.empty()));
                continue;
            }
            futures.add(CompletableFuture.supplyAsync(
                    () -> fetchList(peer, pathAndQuery, headers, listType), executor));
        }

        List<T> items = new ArrayList<>();
        boolean partial = false;
        for (CompletableFuture<Optional<List<T>>> future : futures) {
            Optional<List<T>> result = future.join();
            if (result.isPresent()) {
                items.addAll(result.get());
            } else {
                partial = true;
            }
        }
        return new PeerCollection<>(items, partial);
    }

    private <T> Optional<List<T>> fetchList(String peer, String pathAndQuery,
                                            Map<String, String> headers, JavaType listType) {
        Optional<PeerResponse> answer = callWithRetry(peer, HttpMethod.GET, pathAndQuery, null, headers);
        if (answer.isEmpty() || !answer.get().isSuccess()) {
            answer.ifPresent(a -> log.warn("Peer {} answered {} for GET {}", peer, a.status(), pathAndQuery));
            return Optional.empty();
        }
        try {
            List<T> list = objectMapper.readValue(answer.get().body(), listType);
            log.info("Peer {} returned {} item(s) for GET {}", peer, list.size(), pathAndQuery);
            return Optional.of(list);
        } catch (IOException e) {
            log.warn("Peer {} returned an unreadable body: {}", peer, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Calls one peer with retry and exponential backoff.
     * Returns empty when the peer is unreachable (timeout, connection refused) or answers 5xx.
     */
    private Optional<PeerResponse> callWithRetry(String peer, HttpMethod method, String pathAndQuery,
                                                 Object body, Map<String, String> headers) {
        AisafeProperties.Forwarding settings = properties.forwarding();
        Duration wait = settings.backoff();
        for (int attempt = 1; attempt <= settings.attempts(); attempt++) {
            try {
                PeerResponse response = call(peer, method, pathAndQuery, body, headers);
                if (response.status() >= 500) {
                    log.warn("Peer {} answered {} for {} {}", peer, response.status(), method, pathAndQuery);
                    return Optional.empty();
                }
                peerDownUntil.remove(peer);
                return Optional.of(response);
            } catch (RestClientException e) {
                log.warn("Peer {} unreachable (attempt {}/{}): {}",
                        peer, attempt, settings.attempts(), e.getMessage());
                if (attempt < settings.attempts()) {
                    sleep(wait);
                    wait = wait.multipliedBy(2);
                }
            }
        }
        markDown(peer);
        return Optional.empty();
    }

    private PeerResponse call(String peer, HttpMethod method, String pathAndQuery,
                              Object body, Map<String, String> headers) {
        RestClient.RequestBodySpec request = restClient.method(method)
                .uri(URI.create(peer + pathAndQuery))
                .headers(h -> headers.forEach(h::set))
                .accept(MediaType.APPLICATION_JSON);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return request.exchange((clientRequest, clientResponse) -> new PeerResponse(
                peer,
                clientResponse.getStatusCode().value(),
                clientResponse.getHeaders().getContentType(),
                clientResponse.getBody().readAllBytes()));
    }

    /** Headers copied from the incoming request (JWT, correlation id) plus the internal-call headers. */
    private Map<String, String> outgoingHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(ForwardingHeaders.FORWARDED_BY, properties.instanceId());
        headers.put(ForwardingHeaders.SERVICE_KEY, properties.serviceKey());

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest incoming = servletAttributes.getRequest();
            String authorization = incoming.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                headers.put(HttpHeaders.AUTHORIZATION, authorization);
            }
            Object correlationId = incoming.getAttribute(ForwardingHeaders.CORRELATION_ATTRIBUTE);
            if (correlationId != null) {
                headers.put(ForwardingHeaders.CORRELATION_ID, correlationId.toString());
            }
        }
        return headers;
    }

    private boolean isMarkedDown(String peer) {
        Instant until = peerDownUntil.get(peer);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            peerDownUntil.remove(peer);
            return false;
        }
        return true;
    }

    private void markDown(String peer) {
        Duration downFor = properties.forwarding().peerDownFor();
        peerDownUntil.put(peer, Instant.now().plus(downFor));
        log.warn("Peer {} marked as down for {} s", peer, downFor.toSeconds());
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
