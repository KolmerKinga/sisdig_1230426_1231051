package pt.isep.aisafe.maintenance.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.aisafe.maintenance.api.DistributedQueries.Aggregated;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.CostReport;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.HoursReport;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.LowStockAlert;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.MaintenanceAlert;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.PartUsage;
import pt.isep.aisafe.maintenance.api.MaintenanceReports.TurnaroundAverage;
import pt.isep.aisafe.maintenance.api.dto.AircraftMaintenanceStatus;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.application.InvalidRequestException;
import pt.isep.aisafe.maintenance.application.RecordFilter;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftClient;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reports and fleet queries. Every report is computed from the records of ALL replicas
 * (aggregated), so the answer is the same whichever replica is asked.
 */
@RestController
@RequestMapping("/api/maintenance")
public class ReportController {

    private static final Set<String> COST_GROUPS = Set.of("aircraft", "model");

    private final DistributedQueries distributed;
    private final AircraftClient aircraftClient;

    public ReportController(DistributedQueries distributed, AircraftClient aircraftClient) {
        this.distributed = distributed;
        this.aircraftClient = aircraftClient;
    }

    /** US219 - ongoing maintenance activities across the fleet. */
    @GetMapping("/ongoing")
    public ResponseEntity<List<RecordResponse>> ongoing() {
        Aggregated<RecordResponse> all = distributed.records(
                new RecordFilter(null, null, MaintenanceStatus.ONGOING, null, null));
        return answer(MaintenanceReports.ongoing(all.items()), all.partial());
    }

    /** US221 - average turnaround time per aircraft model. */
    @GetMapping("/turnaround-average")
    public ResponseEntity<List<TurnaroundAverage>> turnaround() {
        Aggregated<RecordResponse> all = distributed.records(
                new RecordFilter(null, null, MaintenanceStatus.COMPLETED, null, null));
        return answer(MaintenanceReports.turnaroundPerModel(all.items()), all.partial());
    }

    /** US220 - maintenance costs per aircraft (default) or per model. */
    @GetMapping("/reports/costs")
    public ResponseEntity<List<CostReport>> costs(@RequestParam(defaultValue = "aircraft") String groupBy) {
        String group = groupBy.toLowerCase(Locale.ROOT);
        if (!COST_GROUPS.contains(group)) {
            throw new InvalidRequestException("groupBy must be one of " + COST_GROUPS);
        }
        Aggregated<RecordResponse> all = distributed.records(RecordFilter.ALL);
        return answer(MaintenanceReports.costs(all.items(), group), all.partial());
    }

    /** US117 - total maintenance hours per aircraft of the fleet. */
    @GetMapping("/reports/hours")
    public ResponseEntity<List<HoursReport>> hours() {
        Aggregated<RecordResponse> all = distributed.records(RecordFilter.ALL);
        return answer(MaintenanceReports.hoursPerAircraft(all.items()), all.partial());
    }

    /** US222 - aircraft due for maintenance (needs the fleet from aircraft-service). */
    @GetMapping("/alerts")
    public ResponseEntity<List<MaintenanceAlert>> alerts(
            @RequestParam(defaultValue = "180") int calendarDaysThreshold,
            @RequestParam(defaultValue = "500") double flightHoursThreshold) {
        if (calendarDaysThreshold < 0 || flightHoursThreshold < 0) {
            throw new InvalidRequestException("Thresholds cannot be negative");
        }
        Aggregated<RecordResponse> completed = distributed.records(
                new RecordFilter(null, null, MaintenanceStatus.COMPLETED, null, null));
        return answer(MaintenanceReports.alerts(aircraftClient.listAircraft(), completed.items(),
                calendarDaysThreshold, flightHoursThreshold, LocalDate.now()), completed.partial());
    }

    /** US226 - parts used in maintenance. */
    @GetMapping("/parts/inventory")
    public ResponseEntity<List<PartUsage>> partsInventory() {
        Aggregated<RecordResponse> all = distributed.records(RecordFilter.ALL);
        return answer(MaintenanceReports.partsUsage(all.items()), all.partial());
    }

    /** US226 - parts used at least {@code threshold} units. */
    @GetMapping("/parts/low-stock")
    public ResponseEntity<List<LowStockAlert>> lowStock(@RequestParam(defaultValue = "10") int threshold) {
        if (threshold < 1) {
            throw new InvalidRequestException("threshold must be at least 1");
        }
        Aggregated<RecordResponse> all = distributed.records(RecordFilter.ALL);
        return answer(MaintenanceReports.lowStock(all.items(), threshold), all.partial());
    }

    /**
     * Contract for route-flight-service: is the aircraft under ongoing maintenance on any replica?
     * {@code complete=false} means a replica could not be asked, so the caller should not trust a
     * "false" answer for safety-critical decisions (e.g. scheduling a flight).
     */
    @GetMapping("/aircraft/{registration}/ongoing")
    public ResponseEntity<AircraftMaintenanceStatus> aircraftOngoing(@PathVariable String registration) {
        Aggregated<RecordResponse> ongoing = distributed.records(
                RecordFilter.forAircraft(registration, MaintenanceStatus.ONGOING));
        AircraftMaintenanceStatus body = new AircraftMaintenanceStatus(
                registration.trim().toUpperCase(Locale.ROOT),
                !ongoing.items().isEmpty(),
                ongoing.items().stream().map(RecordResponse::id).toList(),
                !ongoing.partial());
        return answer(body, ongoing.partial());
    }

    private static <T> ResponseEntity<T> answer(T body, boolean partial) {
        return ResponseEntity.ok()
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(partial))
                .body(body);
    }
}
