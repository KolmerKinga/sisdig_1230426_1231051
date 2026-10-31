package pt.isep.aisafe.routeflight.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pt.isep.aisafe.routeflight.domain.RouteType;

public record CreateRouteRequest(
        @NotBlank(message = "Route name is required")
        String routeName,

        @NotNull(message = "Route type is required")
        RouteType type,

        @NotBlank(message = "Origin IATA code is required")
        String originIata,

        @NotBlank(message = "Destination IATA code is required")
        String destinationIata,

        @NotNull(message = "Estimated flight time is required")
        @Positive(message = "Estimated flight time must be positive")
        Double estimatedTimeHours,

        @NotNull(message = "Distance is required")
        @Positive(message = "Distance must be positive")
        Double distanceKm,

        @Positive(message = "Required range must be positive")
        Double requiredRange,

        @Positive(message = "Required capacity must be positive")
        Integer requiredCapacity
) {}
