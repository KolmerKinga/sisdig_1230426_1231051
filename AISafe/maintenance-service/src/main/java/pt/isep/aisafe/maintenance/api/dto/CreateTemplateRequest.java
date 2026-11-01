package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;
import pt.isep.aisafe.maintenance.domain.MaintenanceTemplate;
import pt.isep.aisafe.maintenance.domain.MaintenanceType;

import java.util.Map;

/**
 * Body of POST /api/maintenance/templates (PSOFT US115). Same field names as PSOFT.
 * {@code templateChecklist} maps each task to true (required) or false (optional).
 */
public record CreateTemplateRequest(
        @NotBlank String name,
        @NotNull @Positive Double expectedDuration,
        @NotEmpty Map<String, Boolean> templateChecklist,
        @NotNull MaintenanceType operation,
        @NotNull MaintenanceAttribute attribute) {

    public MaintenanceTemplate toDomain() {
        return new MaintenanceTemplate(name, operation, attribute, expectedDuration, templateChecklist);
    }
}
