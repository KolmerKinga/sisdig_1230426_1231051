package pt.isep.aisafe.maintenance.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintenanceRecordTest {

    private static Map<String, Boolean> checklist() {
        Map<String, Boolean> items = new LinkedHashMap<>();
        items.put("Inspect fuselage", true);
        items.put("Check landing gear", true);
        items.put("Test cabin lights", false);
        return items;
    }

    private static MaintenanceRecord planned() {
        return new MaintenanceRecord("cs-tma", "A320-200", UUID.randomUUID(), "A-Check",
                MaintenanceType.INSPECTION, MaintenanceAttribute.AIRFRAME, checklist(),
                "Routine A-Check", LocalDate.of(2026, 11, 1), 8.0, "technician");
    }

    @Test
    void newRecordIsPlannedWithTheTemplateChecklist() {
        MaintenanceRecord record = planned();

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.INLINE);
        assertThat(record.getAircraftRegistration()).isEqualTo("CS-TMA");
        assertThat(record.getChecklist()).hasSize(3);
    }

    @Test
    void startOnlyFromInline() {
        MaintenanceRecord record = planned();
        record.start();

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.ONGOING);
        assertThatThrownBy(record::start).isInstanceOf(MaintenanceRuleException.class);
    }

    @Test
    void cannotCompleteWithRequiredTaskMissing() {
        MaintenanceRecord record = planned();
        record.start();

        assertThatThrownBy(() -> record.complete("x", Map.of("Inspect fuselage", true),
                LocalDate.of(2026, 11, 2), "technician"))
                .isInstanceOf(MaintenanceRuleException.class);
        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.ONGOING);
    }

    @Test
    void completesWhenRequiredTasksAreDone() {
        MaintenanceRecord record = planned();
        record.start();

        record.complete("ok", Map.of("Inspect fuselage", true, "Check landing gear", true),
                LocalDate.of(2026, 11, 2), "supervisor");

        assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
        assertThat(record.getCompletedBy()).isEqualTo("supervisor");
        assertThat(record.getChecklist().get(0).isDone()).isTrue();
        assertThat(record.getChecklist().get(2).isDone()).isFalse();
        assertThatThrownBy(() -> record.complete("again", Map.of(), LocalDate.of(2026, 11, 3), "x"))
                .isInstanceOf(MaintenanceRuleException.class);
    }

    @Test
    void rejectsEndBeforeStartAndUnknownTasks() {
        MaintenanceRecord record = planned();

        assertThatThrownBy(() -> record.complete("x", Map.of(), LocalDate.of(2026, 10, 1), "t"))
                .isInstanceOf(InvalidMaintenanceException.class);
        assertThatThrownBy(() -> record.complete("x", Map.of("Wash the plane", true), LocalDate.of(2026, 11, 2), "t"))
                .isInstanceOf(InvalidMaintenanceException.class);
    }

    @Test
    void partsAndCost() {
        MaintenanceRecord record = planned();
        record.addUsedPart(new UsedPart("FLT-OIL-01", 3, 45.0));
        record.addUsedPart(new UsedPart("BRK-PAD", 1, 350.0));

        assertThat(record.totalCost()).isEqualTo(485.0);
        assertThatThrownBy(() -> new UsedPart("X", 0, 1.0)).isInstanceOf(InvalidMaintenanceException.class);
    }

    @Test
    void templateValidation() {
        assertThatThrownBy(() -> new MaintenanceTemplate("T", MaintenanceType.INSPECTION, MaintenanceAttribute.ENGINE,
                2.0, Map.of())).isInstanceOf(InvalidMaintenanceException.class);
        assertThatThrownBy(() -> new MaintenanceTemplate("T", MaintenanceType.INSPECTION, MaintenanceAttribute.ENGINE,
                0.0, Map.of("a", true))).isInstanceOf(InvalidMaintenanceException.class);
    }
}
