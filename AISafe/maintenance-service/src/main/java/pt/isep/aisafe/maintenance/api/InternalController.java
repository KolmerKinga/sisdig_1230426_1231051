package pt.isep.aisafe.maintenance.api;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.api.dto.TemplateResponse;
import pt.isep.aisafe.maintenance.application.MaintenanceService;
import pt.isep.aisafe.maintenance.application.RecordFilter;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints used ONLY between replicas of maintenance-service (peer aggregation).
 *
 * <p>They return the LOCAL data of this replica, never forward, and are only accessible with a
 * valid service key (see SecurityConfig). This is what lets a replica compute a report with the
 * records of every replica, whatever the role of the user who asked for the report.</p>
 */
@RestController
@RequestMapping("/api/maintenance/internal")
public class InternalController {

    private final MaintenanceService service;

    public InternalController(MaintenanceService service) {
        this.service = service;
    }

    @GetMapping("/records")
    public List<RecordResponse> localRecords(
            @RequestParam(required = false) String aircraft,
            @RequestParam(required = false) MaintenanceAttribute attribute,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate initialDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate finalDate) {
        return service.searchRecords(new RecordFilter(aircraft, attribute, status, initialDate, finalDate)).stream()
                .map(RecordResponse::from)
                .toList();
    }

    @GetMapping("/templates")
    public List<TemplateResponse> localTemplates() {
        return service.allTemplates().stream().map(TemplateResponse::from).toList();
    }
}
