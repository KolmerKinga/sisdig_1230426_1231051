package pt.isep.aisafe.maintenance.application;

import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Search criteria for maintenance records (PSOFT US116, US218). Every field is optional.
 * The same filter is sent to the peers, so each replica filters its own records.
 */
public record RecordFilter(String aircraft, MaintenanceAttribute attribute, MaintenanceStatus status,
                           LocalDate initialDate, LocalDate finalDate) {

    public static final RecordFilter ALL = new RecordFilter(null, null, null, null, null);

    public RecordFilter {
        aircraft = aircraft == null || aircraft.isBlank() ? null : aircraft.trim().toUpperCase(Locale.ROOT);
        if (initialDate != null && finalDate != null && finalDate.isBefore(initialDate)) {
            throw new InvalidRequestException("finalDate cannot be before initialDate");
        }
    }

    public static RecordFilter forAircraft(String registration, MaintenanceStatus status) {
        return new RecordFilter(registration, null, status, null, null);
    }

    /** Query string with the non-empty criteria, e.g. "aircraft=CS-TMA&status=ONGOING" (may be empty). */
    public String toQueryString() {
        List<String> parts = new ArrayList<>();
        add(parts, "aircraft", aircraft);
        add(parts, "attribute", attribute == null ? null : attribute.name());
        add(parts, "status", status == null ? null : status.name());
        add(parts, "initialDate", initialDate == null ? null : initialDate.toString());
        add(parts, "finalDate", finalDate == null ? null : finalDate.toString());
        return String.join("&", parts);
    }

    private static void add(List<String> parts, String name, String value) {
        if (value != null) {
            parts.add(name + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }
}
