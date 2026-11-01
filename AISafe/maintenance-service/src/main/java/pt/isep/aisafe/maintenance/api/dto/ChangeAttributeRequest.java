package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotNull;
import pt.isep.aisafe.maintenance.domain.MaintenanceAttribute;

/** Body of PATCH /api/maintenance/records/{id} (PSOFT US217). */
public record ChangeAttributeRequest(@NotNull MaintenanceAttribute attribute) {
}
