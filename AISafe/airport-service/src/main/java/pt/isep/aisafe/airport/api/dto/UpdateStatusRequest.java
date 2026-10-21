package pt.isep.aisafe.airport.api.dto;

import jakarta.validation.constraints.NotNull;
import pt.isep.aisafe.airport.domain.AirportStatus;

/**
 * Body of PATCH /api/airports/{iataCode}/status (PSOFT US109).
 */
public record UpdateStatusRequest(@NotNull AirportStatus status) {
}
