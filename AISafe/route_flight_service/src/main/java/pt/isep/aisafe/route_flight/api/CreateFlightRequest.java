package pt.isep.aisafe.routeflight.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateFlightRequest(
        @NotNull(message = "Route ID is required")
        Long routeId,

        @NotBlank(message = "Aircraft registration is required")
        String aircraftRegistration,

        @NotNull(message = "Scheduled departure time is required")
        LocalDateTime scheduledDeparture,

        @NotNull(message = "Scheduled arrival time is required")
        LocalDateTime scheduledArrival
) {}
