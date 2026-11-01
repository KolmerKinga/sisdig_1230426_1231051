package pt.isep.aisafe.maintenance.infrastructure.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.aisafe.maintenance.shared.config.AisafeProperties;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;
import pt.isep.aisafe.maintenance.shared.forwarding.PeerResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP client for aircraft-service (Maintenance -> Aircraft, Customer/Supplier).
 *
 * <ul>
 *   <li>Knows the URLs of BOTH aircraft-service replicas (aisafe.services.aircraft) and fails over
 *       to the next one when a replica does not answer, answers 5xx or does not have the aircraft.</li>
 *   <li>Propagates the user's JWT, the service key and the correlation id. It does NOT send
 *       X-Forwarded-By: this is a call to another service, not peer forwarding, so the aircraft
 *       replica that receives it may still forward to its own peer.</li>
 *   <li>Timeouts come from the shared HTTP client (connect 1 s, read 2 s).</li>
 * </ul>
 *
 * <p>Contract used (aircraft-service): GET /api/aircraft/{registration}, GET /api/aircraft,
 * PATCH /api/aircraft/{registration}/status with body {"status": "MAINTENANCE" | "AVAILABLE"}.</p>
 */
@Component
public class AircraftClient {

    private static final Logger log = LoggerFactory.getLogger(AircraftClient.class);
    private static final String BASE_PATH = "/api/aircraft";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final List<String> baseUrls;
    private final String serviceKey;

    public AircraftClient(RestClient peerRestClient, ObjectMapper objectMapper, AisafeProperties properties,
                          @Value("${aisafe.services.aircraft}") List<String> aircraftUrls) {
        this.restClient = peerRestClient;
        this.objectMapper = objectMapper;
        this.serviceKey = properties.serviceKey();
        this.baseUrls = aircraftUrls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(u -> u.trim().endsWith("/") ? u.trim().substring(0, u.trim().length() - 1) : u.trim())
                .toList();
    }

    /** Aircraft data, or AircraftNotFoundException / DependencyUnavailableException. */
    public AircraftInfo getAircraft(String registrationNumber) {
        PeerResponse answer = call(HttpMethod.GET, BASE_PATH + "/" + encode(registrationNumber), null)
                .orElseThrow(() -> new AircraftNotFoundException(registrationNumber));
        return read(answer, objectMapper.getTypeFactory().constructType(AircraftInfo.class));
    }

    /** Changes the availability of the aircraft (MAINTENANCE when work starts, AVAILABLE when it ends). */
    public AircraftInfo updateStatus(String registrationNumber, String status) {
        PeerResponse answer = call(HttpMethod.PATCH, BASE_PATH + "/" + encode(registrationNumber) + "/status",
                Map.of("status", status))
                .orElseThrow(() -> new AircraftNotFoundException(registrationNumber));
        return read(answer, objectMapper.getTypeFactory().constructType(AircraftInfo.class));
    }

    /** Whole fleet (aircraft-service aggregates its own replicas). Used by the maintenance alerts. */
    public List<AircraftInfo> listAircraft() {
        PeerResponse answer = call(HttpMethod.GET, BASE_PATH, null)
                .orElseThrow(() -> new DependencyUnavailableException("aircraft-service has no aircraft list"));
        return read(answer, objectMapper.getTypeFactory().constructCollectionType(List.class, AircraftInfo.class));
    }

    /**
     * Tries every aircraft-service replica in order.
     * Returns the first 2xx answer; empty if every reachable replica answered 404.
     */
    private Optional<PeerResponse> call(HttpMethod method, String path, Object body) {
        if (baseUrls.isEmpty()) {
            throw new DependencyUnavailableException("aircraft-service URLs are not configured (aisafe.services.aircraft)");
        }
        Map<String, String> headers = outgoingHeaders();
        boolean someoneSaidNotFound = false;
        for (String baseUrl : baseUrls) {
            try {
                PeerResponse answer = exchange(baseUrl, method, path, body, headers);
                if (answer.isSuccess()) {
                    log.info("aircraft-service {} answered {} for {} {}", baseUrl, answer.status(), method, path);
                    return Optional.of(answer);
                }
                if (answer.isNotFound()) {
                    someoneSaidNotFound = true;
                    continue;
                }
                if (answer.status() >= 500) {
                    log.warn("aircraft-service {} answered {} for {} {}; trying next replica",
                            baseUrl, answer.status(), method, path);
                    continue;
                }
                throw new DependencyRejectedException("aircraft-service rejected " + method + " " + path
                        + " with HTTP " + answer.status());
            } catch (RestClientException e) {
                log.warn("aircraft-service {} unreachable for {} {}: {}", baseUrl, method, path, e.getMessage());
            }
        }
        if (someoneSaidNotFound) {
            return Optional.empty();
        }
        throw new DependencyUnavailableException("aircraft-service is unavailable (no replica answered)");
    }

    private PeerResponse exchange(String baseUrl, HttpMethod method, String path, Object body,
                                  Map<String, String> headers) {
        RestClient.RequestBodySpec request = restClient.method(method)
                .uri(URI.create(baseUrl + path))
                .headers(h -> headers.forEach(h::set))
                .accept(MediaType.APPLICATION_JSON);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return request.exchange((clientRequest, clientResponse) -> new PeerResponse(
                baseUrl,
                clientResponse.getStatusCode().value(),
                clientResponse.getHeaders().getContentType(),
                clientResponse.getBody().readAllBytes()));
    }

    private <T> T read(PeerResponse answer, JavaType type) {
        try {
            return objectMapper.readValue(answer.body(), type);
        } catch (IOException e) {
            throw new DependencyRejectedException("aircraft-service returned an unexpected body: " + e.getMessage());
        }
    }

    /** JWT and correlation id copied from the incoming request, plus the service key. */
    private Map<String, String> outgoingHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(ForwardingHeaders.SERVICE_KEY, serviceKey);
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

    private static String encode(String value) {
        return URLEncoder.encode(value.trim(), StandardCharsets.UTF_8).replace("+", "%20");
    }
}
