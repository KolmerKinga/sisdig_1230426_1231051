package pt.isep.aisafe.routeflight.api.dto;

import pt.isep.aisafe.routeflight.domain.Route;
import pt.isep.aisafe.routeflight.domain.RouteStatus;
import pt.isep.aisafe.routeflight.domain.RouteType;

import java.time.LocalDate;

public record RouteResponse(
        Long routeId,
        String routeName,
        RouteType type,
        RouteStatus status,
        String originAirportIata,
        String destinationAirportIata,
        Double estimatedFlightTimeHours,
        Double distanceKm,
        LocalDate routeBegin,
        LocalDate routeFinish,
        Integer routeUsage,
        Double requiredRange,
        Integer requiredCapacity
) {
    public static RouteResponse fromDomain(Route route) {
        return new RouteResponse(
                route.getRouteId(),
                route.getRouteName(),
                route.getType(),
                route.getStatus(),
                route.getOriginAirportIata(),
                route.getDestinationAirportIata(),
                route.getEstimatedFlightTimeHours(),
                route.getDistanceKm(),
                route.getRouteHistory() != null ? route.getRouteHistory().getRouteBegin() : null,
                route.getRouteHistory() != null ? route.getRouteHistory().getRouteFinish() : null,
                route.getRouteHistory() != null ? route.getRouteHistory().getRouteUsage() : null,
                route.getRouteRequirements() != null ? route.getRouteRequirements().getRequiredRange() : null,
                route.getRouteRequirements() != null ? route.getRouteRequirements().getRequiredCapacity() : null
        );
    }
}
