package pt.isep.aisafe.maintenance.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.api.dto.TemplateResponse;
import pt.isep.aisafe.maintenance.application.MaintenanceService;
import pt.isep.aisafe.maintenance.application.RecordFilter;
import pt.isep.aisafe.maintenance.application.TemplateNotFoundException;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;
import pt.isep.aisafe.maintenance.shared.forwarding.PeerClient;
import pt.isep.aisafe.maintenance.shared.forwarding.PeerCollection;
import pt.isep.aisafe.maintenance.shared.forwarding.PeerLookup;
import pt.isep.aisafe.maintenance.shared.forwarding.PeerResponse;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Distribution rules of maintenance-service, used by every controller.
 *
 * <ul>
 *   <li><b>Single resource</b> (record or template by id, commands on a record): local store first;
 *       if it is not here, the same request is sent to the peers and the answer of the replica that
 *       holds it is returned. A forwarded request is never forwarded again (max 1 hop).</li>
 *   <li><b>Collections and reports</b>: local data + the data of every peer, read from the peers'
 *       internal endpoints (/api/maintenance/internal/**), merged by id. Internal endpoints only
 *       return local data, so there are no chains of calls.</li>
 * </ul>
 */
@Component
public class DistributedQueries {

    static final String INTERNAL_RECORDS = "/api/maintenance/internal/records";
    static final String INTERNAL_TEMPLATES = "/api/maintenance/internal/templates";
    static final String TEMPLATES = "/api/maintenance/templates/";

    /** Aggregated result and whether a peer could not be contacted. */
    public record Aggregated<T>(List<T> items, boolean partial) {
    }

    private final MaintenanceService service;
    private final PeerClient peerClient;
    private final ObjectMapper objectMapper;

    public DistributedQueries(MaintenanceService service, PeerClient peerClient, ObjectMapper objectMapper) {
        this.service = service;
        this.peerClient = peerClient;
        this.objectMapper = objectMapper;
    }

    /** Records matching the filter on ALL replicas, without duplicates, most recent first. */
    public Aggregated<RecordResponse> records(RecordFilter filter) {
        List<RecordResponse> local = service.searchRecords(filter).stream().map(RecordResponse::from).toList();
        String query = filter.toQueryString();
        PeerCollection<RecordResponse> remote = peerClient.collectFromPeers(
                query.isEmpty() ? INTERNAL_RECORDS : INTERNAL_RECORDS + "?" + query, RecordResponse.class);
        List<RecordResponse> merged = merge(local, remote.items(), RecordResponse::id).stream()
                .sorted(Comparator.comparing(RecordResponse::startDate).reversed()
                        .thenComparing(r -> r.id().toString()))
                .toList();
        return new Aggregated<>(merged, remote.partial());
    }

    /** Templates of ALL replicas, without duplicates, sorted by name. */
    public Aggregated<TemplateResponse> templates() {
        List<TemplateResponse> local = service.allTemplates().stream().map(TemplateResponse::from).toList();
        PeerCollection<TemplateResponse> remote = peerClient.collectFromPeers(INTERNAL_TEMPLATES, TemplateResponse.class);
        List<TemplateResponse> merged = merge(local, remote.items(), TemplateResponse::id).stream()
                .sorted(Comparator.comparing(TemplateResponse::name).thenComparing(t -> t.id().toString()))
                .toList();
        return new Aggregated<>(merged, remote.partial());
    }

    /** A template from this replica or from the peer that holds it (used when creating a record). */
    public TemplateResponse template(UUID id) {
        return service.findTemplate(id)
                .map(TemplateResponse::from)
                .orElseGet(() -> {
                    PeerLookup lookup = peerClient.forwardToPeers(HttpMethod.GET, TEMPLATES + id, null);
                    if (!lookup.found()) {
                        throw new TemplateNotFoundException(id, lookup.partial());
                    }
                    try {
                        return objectMapper.readValue(lookup.response().get().body(), TemplateResponse.class);
                    } catch (IOException e) {
                        throw new TemplateNotFoundException(id, true);
                    }
                });
    }

    /**
     * The resource is not on this replica: ask the peers and pass through the answer of the replica
     * that holds it. {@code notFound} builds the 404 exception (argument: partial).
     */
    public ResponseEntity<?> answerFromPeers(HttpMethod method, HttpServletRequest http, Object body,
                                             Function<Boolean, RuntimeException> notFound) {
        if (ForwardingHeaders.isForwarded(http)) {
            throw notFound.apply(false);
        }
        PeerLookup lookup = peerClient.forwardToPeers(method, ForwardingHeaders.pathAndQuery(http), body);
        if (lookup.response().isEmpty()) {
            throw notFound.apply(lookup.partial());
        }
        PeerResponse answer = lookup.response().get();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(answer.status())
                .header(ForwardingHeaders.DATA_SOURCE, answer.peer())
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(lookup.partial()));
        if (answer.contentType() != null) {
            builder.contentType(answer.contentType());
        }
        return builder.body(answer.body());
    }

    /** Local items win over peer copies with the same id. */
    private static <T, K> List<T> merge(List<T> local, List<T> remote, Function<T, K> id) {
        Map<K, T> byId = new LinkedHashMap<>();
        local.forEach(item -> byId.putIfAbsent(id.apply(item), item));
        remote.forEach(item -> byId.putIfAbsent(id.apply(item), item));
        return List.copyOf(byId.values());
    }
}
