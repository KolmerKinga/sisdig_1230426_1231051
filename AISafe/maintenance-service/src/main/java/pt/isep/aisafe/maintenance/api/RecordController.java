package pt.isep.aisafe.maintenance.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.aisafe.maintenance.api.DistributedQueries.Aggregated;
import pt.isep.aisafe.maintenance.api.dto.AddPartRequest;
import pt.isep.aisafe.maintenance.api.dto.ChangeAttributeRequest;
import pt.isep.aisafe.maintenance.api.dto.CompleteRecordRequest;
import pt.isep.aisafe.maintenance.api.dto.CreateRecordRequest;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.api.dto.TemplateResponse;
import pt.isep.aisafe.maintenance.api.dto.UpdateRecordStatusRequest;
import pt.isep.aisafe.maintenance.application.InvalidRequestException;
import pt.isep.aisafe.maintenance.application.MaintenanceService;
import pt.isep.aisafe.maintenance.application.RecordFilter;
import pt.isep.aisafe.maintenance.application.RecordNotFoundException;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceRecord;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftClient;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftInfo;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Maintenance records (PSOFT US115, US116, US119, US217, US218).
 *
 * <ul>
 *   <li>A new record is stored on the replica that receives it. The aircraft must exist in
 *       aircraft-service; the template may be on any replica.</li>
 *   <li>Commands on a record (start, complete, parts, attribute) run on the replica that holds it;
 *       other replicas forward them.</li>
 *   <li>Starting a record puts the aircraft in MAINTENANCE in aircraft-service; completing it puts
 *       the aircraft back to AVAILABLE when no other ongoing maintenance exists. The result of that
 *       call is returned in {@code aircraftSync}: the maintenance data is saved even if
 *       aircraft-service is down (Maintenance owns its data; the aircraft status catches up later).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/maintenance")
public class RecordController {

    private static final Logger log = LoggerFactory.getLogger(RecordController.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final MaintenanceService service;
    private final DistributedQueries distributed;
    private final AircraftClient aircraftClient;

    public RecordController(MaintenanceService service, DistributedQueries distributed, AircraftClient aircraftClient) {
        this.service = service;
        this.distributed = distributed;
        this.aircraftClient = aircraftClient;
    }

    // ------------------------------------------------------------------ create

    /** US115 - plan a maintenance (status INLINE). "/record" is kept for compatibility with PSOFT. */
    @PostMapping(value = {"/records", "/record"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecordResponse> create(@Valid @RequestBody CreateRecordRequest request,
                                                 Authentication authentication) {
        TemplateResponse template = distributed.template(request.templateId());
        AircraftInfo aircraft = aircraftClient.getAircraft(request.registrationNumber());

        MaintenanceRecord record = service.createRecord(new MaintenanceRecord(
                aircraft.registrationNumber() == null ? request.registrationNumber() : aircraft.registrationNumber(),
                aircraft.modelName(),
                template.id(),
                template.name(),
                template.operation(),
                template.attribute(),
                template.templateChecklist(),
                request.description(),
                request.startDate(),
                request.durationHours() != null ? request.durationHours() : template.expectedDuration(),
                authentication.getName()));

        return ResponseEntity.created(URI.create("/api/maintenance/records/" + record.getId()))
                .header(ForwardingHeaders.DATA_SOURCE, "local")
                .body(RecordResponse.from(record));
    }

    // ------------------------------------------------------------------ queries

    @GetMapping("/records/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id, HttpServletRequest http) {
        return service.findRecord(id)
                .<ResponseEntity<?>>map(r -> local(RecordResponse.from(r)))
                .orElseGet(() -> distributed.answerFromPeers(HttpMethod.GET, http, null,
                        partial -> new RecordNotFoundException(id, partial)));
    }

    /** US116 / US218 - search by aircraft, component, status and/or start-date range (all replicas). */
    @GetMapping("/records")
    public ResponseEntity<List<RecordResponse>> search(
            @RequestParam(required = false) String aircraft,
            @RequestParam(required = false) MaintenanceAttribute attribute,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate initialDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("page must be >= 0 and size between 1 and " + MAX_PAGE_SIZE);
        }
        Aggregated<RecordResponse> all = distributed.records(
                new RecordFilter(aircraft, attribute, status, initialDate, finalDate));
        int from = Math.min(page * size, all.items().size());
        int to = Math.min(from + size, all.items().size());
        return ResponseEntity.ok()
                .header(ForwardingHeaders.TOTAL_COUNT, String.valueOf(all.items().size()))
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(all.partial()))
                .body(all.items().subList(from, to));
    }

    // ------------------------------------------------------------------ commands

    /** US119 - start the work (only ONGOING is accepted; use /complete to finish). */
    @PatchMapping(value = "/records/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeStatus(@PathVariable UUID id, @Valid @RequestBody UpdateRecordStatusRequest request,
                                          HttpServletRequest http) {
        if (request.status() == MaintenanceStatus.COMPLETED) {
            throw new InvalidRequestException("Use POST /api/maintenance/records/{id}/complete to complete a record");
        }
        if (request.status() != MaintenanceStatus.ONGOING) {
            throw new InvalidRequestException("Only the ONGOING status can be set with this endpoint");
        }
        if (!service.recordExists(id)) {
            return distributed.answerFromPeers(HttpMethod.PATCH, http, request,
                    partial -> new RecordNotFoundException(id, partial));
        }
        MaintenanceRecord record = service.startRecord(id);
        String sync = syncAircraft(record.getAircraftRegistration(), "MAINTENANCE");
        return local(RecordResponse.from(record, sync));
    }

    /** US119 - complete with notes, checklist result and end date. */
    @PostMapping(value = "/records/{id}/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> complete(@PathVariable UUID id, @Valid @RequestBody CompleteRecordRequest request,
                                      Authentication authentication, HttpServletRequest http) {
        if (!service.recordExists(id)) {
            return distributed.answerFromPeers(HttpMethod.POST, http, request,
                    partial -> new RecordNotFoundException(id, partial));
        }
        MaintenanceRecord record = service.completeRecord(id, request.completionNotes(), request.tasksDone(),
                request.endDate(), authentication.getName());
        String sync = releaseAircraftIfIdle(record);
        return local(RecordResponse.from(record, sync));
    }

    /** US220 / US226 - register a part used in the maintenance. */
    @PostMapping(value = "/records/{id}/parts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addPart(@PathVariable UUID id, @Valid @RequestBody AddPartRequest request,
                                     HttpServletRequest http) {
        if (!service.recordExists(id)) {
            return distributed.answerFromPeers(HttpMethod.POST, http, request,
                    partial -> new RecordNotFoundException(id, partial));
        }
        return local(RecordResponse.from(service.addUsedPart(id, request.toDomain())));
    }

    /** US217 - categorize the record by maintenance component. */
    @PatchMapping(value = "/records/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeAttribute(@PathVariable UUID id, @Valid @RequestBody ChangeAttributeRequest request,
                                             HttpServletRequest http) {
        if (!service.recordExists(id)) {
            return distributed.answerFromPeers(HttpMethod.PATCH, http, request,
                    partial -> new RecordNotFoundException(id, partial));
        }
        return local(RecordResponse.from(service.changeAttribute(id, request.attribute())));
    }

    // ------------------------------------------------------------------ aircraft-service synchronisation

    /**
     * Puts the aircraft back to AVAILABLE only when no other ONGOING maintenance exists for it on any
     * replica. If a replica cannot be asked, the aircraft is NOT released (safety first).
     */
    private String releaseAircraftIfIdle(MaintenanceRecord completed) {
        Aggregated<RecordResponse> ongoing = distributed.records(
                RecordFilter.forAircraft(completed.getAircraftRegistration(), MaintenanceStatus.ONGOING));
        boolean otherOngoing = ongoing.items().stream().anyMatch(r -> !r.id().equals(completed.getId()));
        if (otherOngoing) {
            return "SKIPPED: aircraft still has ongoing maintenance";
        }
        if (ongoing.partial()) {
            return "SKIPPED: could not confirm with every replica that no other maintenance is ongoing";
        }
        return syncAircraft(completed.getAircraftRegistration(), "AVAILABLE");
    }

    private String syncAircraft(String registration, String status) {
        try {
            aircraftClient.updateStatus(registration, status);
            return "UPDATED: aircraft " + registration + " is now " + status;
        } catch (RuntimeException e) {
            log.warn("Could not set aircraft {} to {}: {}", registration, status, e.getMessage());
            return "FAILED: " + e.getMessage();
        }
    }

    private static ResponseEntity<RecordResponse> local(RecordResponse body) {
        return ResponseEntity.ok().header(ForwardingHeaders.DATA_SOURCE, "local").body(body);
    }
}
