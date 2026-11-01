package pt.isep.aisafe.maintenance.api.dto;

import jakarta.validation.constraints.NotNull;
import pt.isep.aisafe.maintenance.domain.MaintenanceStatus;

/** Body of PATCH /api/maintenance/records/{id}/status. Only ONGOING is accepted here. */
public record UpdateRecordStatusRequest(@NotNull MaintenanceStatus status) {
}
