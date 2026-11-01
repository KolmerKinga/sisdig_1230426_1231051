package pt.isep.aisafe.maintenance.api;

import pt.isep.aisafe.maintenance.api.dto.RecordResponse;
import pt.isep.aisafe.maintenance.api.dto.RecordResponse.UsedPartResponse;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftInfo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Maintenance reports (PSOFT US117, US219, US220, US221, US222, US226).
 *
 * <p>Pure functions over the list of records AGGREGATED from every replica, so the result is the
 * same whichever replica answers. No database or network access here.</p>
 */
public final class MaintenanceReports {

    private static final String UNKNOWN_MODEL = "UNKNOWN";

    public record TurnaroundAverage(String aircraftModel, Double averageDays, long completedRecords) {
    }

    public record CostReport(String group, Double totalCost) {
    }

    public record HoursReport(String aircraftRegistration, Double totalMaintenanceHours, long records) {
    }

    public record MaintenanceAlert(String registrationNumber, String aircraftModel, Double totalFlightHours,
                                   LocalDate lastMaintenanceDate, Long daysSinceLastMaintenance,
                                   String alertReason) {
    }

    public record PartUsage(String partSerialNumber, Integer totalQuantityUsed, Double totalCost,
                            Long timesUsedInRecords) {
    }

    public record LowStockAlert(String partSerialNumber, Integer totalQuantityUsed, Long timesUsedInRecords,
                                String alertMessage) {
    }

    private MaintenanceReports() {
    }

    /** US219 - ongoing maintenance activities across the fleet. */
    public static List<RecordResponse> ongoing(List<RecordResponse> records) {
        return records.stream()
                .filter(r -> r.status() == MaintenanceStatus.ONGOING)
                .sorted(Comparator.comparing(RecordResponse::startDate).thenComparing(RecordResponse::aircraftRegistration))
                .toList();
    }

    /** US221 - average turnaround (days between start and end) per aircraft model, completed records only. */
    public static List<TurnaroundAverage> turnaroundPerModel(List<RecordResponse> records) {
        Map<String, List<Long>> daysPerModel = records.stream()
                .filter(r -> r.status() == MaintenanceStatus.COMPLETED && r.endDate() != null)
                .collect(Collectors.groupingBy(MaintenanceReports::model, TreeMap::new,
                        Collectors.mapping(r -> ChronoUnit.DAYS.between(r.startDate(), r.endDate()),
                                Collectors.toList())));
        return daysPerModel.entrySet().stream()
                .map(e -> new TurnaroundAverage(e.getKey(),
                        round(e.getValue().stream().mapToLong(Long::longValue).average().orElse(0)),
                        e.getValue().size()))
                .toList();
    }

    /** US220 - total cost of used parts per aircraft ("aircraft") or per aircraft model ("model"). */
    public static List<CostReport> costs(List<RecordResponse> records, String groupBy) {
        boolean byModel = "model".equalsIgnoreCase(groupBy);
        Map<String, Double> totals = records.stream()
                .collect(Collectors.groupingBy(r -> byModel ? model(r) : r.aircraftRegistration(), TreeMap::new,
                        Collectors.summingDouble(r -> r.parts().stream().mapToDouble(UsedPartResponse::cost).sum())));
        return totals.entrySet().stream()
                .map(e -> new CostReport(e.getKey(), round(e.getValue())))
                .toList();
    }

    /** US117 - total maintenance hours per aircraft of the fleet. */
    public static List<HoursReport> hoursPerAircraft(List<RecordResponse> records) {
        Map<String, List<RecordResponse>> byAircraft = records.stream()
                .collect(Collectors.groupingBy(RecordResponse::aircraftRegistration, TreeMap::new, Collectors.toList()));
        return byAircraft.entrySet().stream()
                .map(e -> new HoursReport(e.getKey(),
                        round(e.getValue().stream()
                                .map(RecordResponse::durationHours)
                                .filter(Objects::nonNull)
                                .mapToDouble(Double::doubleValue)
                                .sum()),
                        e.getValue().size()))
                .toList();
    }

    /**
     * US222 - aircraft due for maintenance: no completed maintenance, last one older than the
     * calendar threshold, or flight hours above the threshold.
     */
    public static List<MaintenanceAlert> alerts(List<AircraftInfo> fleet, List<RecordResponse> records,
                                                int calendarDaysThreshold, double flightHoursThreshold,
                                                LocalDate today) {
        List<MaintenanceAlert> alerts = new ArrayList<>();
        for (AircraftInfo aircraft : fleet) {
            if (aircraft.registrationNumber() == null) {
                continue;
            }
            Optional<LocalDate> last = records.stream()
                    .filter(r -> r.status() == MaintenanceStatus.COMPLETED && r.endDate() != null)
                    .filter(r -> aircraft.registrationNumber().equalsIgnoreCase(r.aircraftRegistration()))
                    .map(RecordResponse::endDate)
                    .max(Comparator.naturalOrder());

            List<String> reasons = new ArrayList<>();
            Long daysSince = last.map(d -> ChronoUnit.DAYS.between(d, today)).orElse(null);
            if (last.isEmpty()) {
                reasons.add("No completed maintenance on record");
            } else if (daysSince >= calendarDaysThreshold) {
                reasons.add("Last maintenance was " + daysSince + " days ago (threshold: " + calendarDaysThreshold + ")");
            }
            if (aircraft.flightHours() >= flightHoursThreshold) {
                reasons.add("Total flight hours " + aircraft.flightHours() + " reached threshold of "
                        + flightHoursThreshold);
            }
            if (!reasons.isEmpty()) {
                alerts.add(new MaintenanceAlert(aircraft.registrationNumber(), aircraft.modelName(),
                        aircraft.flightHours(), last.orElse(null), daysSince, String.join("; ", reasons)));
            }
        }
        alerts.sort(Comparator.comparing(MaintenanceAlert::registrationNumber));
        return alerts;
    }

    /** US226 - quantity and cost of every part used, across all records. */
    public static List<PartUsage> partsUsage(List<RecordResponse> records) {
        Map<String, List<UsedPartResponse>> bySerial = records.stream()
                .flatMap(r -> r.parts().stream())
                .collect(Collectors.groupingBy(UsedPartResponse::partSerialNumber, TreeMap::new, Collectors.toList()));
        return bySerial.entrySet().stream()
                .map(e -> new PartUsage(e.getKey(),
                        e.getValue().stream().mapToInt(p -> p.quantity() == null ? 0 : p.quantity()).sum(),
                        round(e.getValue().stream().mapToDouble(UsedPartResponse::cost).sum()),
                        (long) e.getValue().size()))
                .toList();
    }

    /** US226 - parts used at least {@code threshold} units: suggest restocking. */
    public static List<LowStockAlert> lowStock(List<RecordResponse> records, int threshold) {
        return partsUsage(records).stream()
                .filter(p -> p.totalQuantityUsed() >= threshold)
                .map(p -> new LowStockAlert(p.partSerialNumber(), p.totalQuantityUsed(), p.timesUsedInRecords(),
                        "Part " + p.partSerialNumber() + " used " + p.totalQuantityUsed() + " units across "
                                + p.timesUsedInRecords() + " record(s); consider restocking (threshold: "
                                + threshold + ")"))
                .toList();
    }

    private static String model(RecordResponse record) {
        return record.aircraftModel() == null || record.aircraftModel().isBlank() ? UNKNOWN_MODEL : record.aircraftModel();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
