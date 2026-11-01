package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of POST /api/maintenance/records (PSOFT US115).
 * {@code durationHours} is optional: when missing, the template's expected duration is used.
 */
public record CreateRecordRequest(
        @NotBlank @Size(max = 20) String registrationNumber,
        @NotNull UUID templateId,
        @NotBlank @Size(max = 1000) String description,
        @NotNull LocalDate startDate,
        @Positive Double durationHours) {
}
