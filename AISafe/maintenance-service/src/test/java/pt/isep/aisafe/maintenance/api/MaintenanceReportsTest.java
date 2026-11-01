package pt.isep.aisafe.maintenance.api;

import org.junit.jupiter.api.Test;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse.UsedPartResponse;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.domain.MaintenanceType;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reports are computed over records that came from DIFFERENT replicas (one list per replica,
 * merged), exactly as the controllers do after aggregation.
 */
class MaintenanceReportsTest {

    private static RecordResponse record(String reg, String model, MaintenanceStatus status, LocalDate start,
                                         LocalDate end, double hours, List<UsedPartResponse> parts) {
        return new RecordResponse(UUID.randomUUID(), reg, model, UUID.randomUUID(), "T", MaintenanceType.INSPECTION,
                MaintenanceAttribute.AIRFRAME, "d", start, end, hours, status, List.of(), null, parts, 0.0,
                "technician", null, null);
    }

    // records stored on replica 1 and on replica 2
    private final List<RecordResponse> replica1 = List.of(
            record("CS-TMA", "A320-200", MaintenanceStatus.COMPLETED, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), 9.5,
                    List.of(new UsedPartResponse("FLT-OIL-01", 6, 45.0), new UsedPartResponse("BRK-PAD", 4, 350.0))),
            record("CS-TMA", "A320-200", MaintenanceStatus.ONGOING, LocalDate.of(2026, 11, 1), null, 6.0, List.of()));
    private final List<RecordResponse> replica2 = List.of(
            record("CS-TMB", "B737-800", MaintenanceStatus.COMPLETED, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20), 130.0,
                    List.of(new UsedPartResponse("FLT-OIL-01", 5, 45.0))));

    private List<RecordResponse> all() {
        return java.util.stream.Stream.concat(replica1.stream(), replica2.stream()).toList();
    }

    @Test
    void lowStockOnlyAppearsWhenReplicasAreAggregated() {
        assertThat(MaintenanceReports.lowStock(replica1, 10)).hasSize(0);
        assertThat(MaintenanceReports.lowStock(replica2, 10)).hasSize(0);

        List<MaintenanceReports.LowStockAlert> alerts = MaintenanceReports.lowStock(all(), 10);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).partSerialNumber()).isEqualTo("FLT-OIL-01");
        assertThat(alerts.get(0).totalQuantityUsed()).isEqualTo(11);
    }

    @Test
    void costsPerModelAndPerAircraft() {
        List<MaintenanceReports.CostReport> byModel = MaintenanceReports.costs(all(), "model");
        assertThat(byModel).hasSize(2);
        assertThat(byModel.get(0).group()).isEqualTo("A320-200");
        assertThat(byModel.get(0).totalCost()).isEqualTo(1670.0);
        assertThat(byModel.get(1).totalCost()).isEqualTo(225.0);

        assertThat(MaintenanceReports.costs(all(), "aircraft").get(1).group()).isEqualTo("CS-TMB");
    }

    @Test
    void turnaroundUsesOnlyCompletedRecords() {
        List<MaintenanceReports.TurnaroundAverage> t = MaintenanceReports.turnaroundPerModel(all());
        assertThat(t).hasSize(2);
        assertThat(t.get(0).averageDays()).isEqualTo(2.0);
        assertThat(t.get(1).averageDays()).isEqualTo(19.0);
    }

    @Test
    void hoursPerAircraftAndOngoing() {
        List<MaintenanceReports.HoursReport> hours = MaintenanceReports.hoursPerAircraft(all());
        assertThat(hours.get(0).totalMaintenanceHours()).isEqualTo(15.5);
        assertThat(hours.get(0).records()).isEqualTo(2L);
        assertThat(MaintenanceReports.ongoing(all())).hasSize(1);
    }

    @Test
    void alertsForOldMaintenanceFlightHoursAndNoHistory() {
        List<AircraftInfo> fleet = List.of(
                new AircraftInfo("CS-TMA", "A320-200", "AVAILABLE", 100.0),
                new AircraftInfo("CS-TMB", "B737-800", "AVAILABLE", 900.0),
                new AircraftInfo("CS-NEW", "A321neo", "AVAILABLE", 10.0));

        List<MaintenanceReports.MaintenanceAlert> alerts =
                MaintenanceReports.alerts(fleet, all(), 180, 500, LocalDate.of(2026, 11, 1));

        assertThat(alerts).hasSize(2);
        assertThat(alerts.get(0).registrationNumber()).isEqualTo("CS-NEW");
        assertThat(alerts.get(1).registrationNumber()).isEqualTo("CS-TMB");
        assertThat(alerts.get(1).daysSinceLastMaintenance()).isEqualTo(316L);
    }
}
