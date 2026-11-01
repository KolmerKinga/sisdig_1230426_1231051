package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;

/**
 * Body of POST /api/maintenance/records/{id}/complete (PSOFT US119).
 * {@code tasksDone} maps each checklist task to true when it was done.
 */
public record CompleteRecordRequest(
        @Size(max = 2000) String completionNotes,
        @NotNull Map<String, Boolean> tasksDone,
        @NotNull LocalDate endDate) {
}
