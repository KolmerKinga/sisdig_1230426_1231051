package pt.isep.aisafe.maintenance.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.isep.aisafe.maintenance.api.DistributedQueries.Aggregated;
import pt.isep.aisafe.maintenance.api.dto.CreateTemplateRequest;
import pt.isep.aisafe.maintenance.api.dto.TemplateResponse;
import pt.isep.aisafe.maintenance.application.MaintenanceService;
import pt.isep.aisafe.maintenance.application.TemplateNotFoundException;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Maintenance templates (PSOFT US115). Templates are stored on the replica that creates them.
 */
@RestController
@RequestMapping("/api/maintenance/templates")
public class TemplateController {

    private final MaintenanceService service;
    private final DistributedQueries distributed;

    public TemplateController(MaintenanceService service, DistributedQueries distributed) {
        this.service = service;
        this.distributed = distributed;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        MaintenanceTemplate template = service.createTemplate(request.toDomain());
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(template.getId()).toUri();
        return ResponseEntity.created(location)
                .header(ForwardingHeaders.DATA_SOURCE, "local")
                .body(TemplateResponse.from(template));
    }

    /** Local store first, then the peers. */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable UUID id, HttpServletRequest http) {
        return service.findTemplate(id)
                .<ResponseEntity<?>>map(t -> ResponseEntity.ok()
                        .header(ForwardingHeaders.DATA_SOURCE, "local")
                        .body(TemplateResponse.from(t)))
                .orElseGet(() -> distributed.answerFromPeers(HttpMethod.GET, http, null,
                        partial -> new TemplateNotFoundException(id, partial)));
    }

    /** All templates of every replica. */
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> list() {
        Aggregated<TemplateResponse> all = distributed.templates();
        return ResponseEntity.ok()
                .header(ForwardingHeaders.TOTAL_COUNT, String.valueOf(all.items().size()))
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(all.partial()))
                .body(all.items());
    }
}
