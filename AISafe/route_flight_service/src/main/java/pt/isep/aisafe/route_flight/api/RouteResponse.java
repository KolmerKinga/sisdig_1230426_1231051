package pt.isep.aisafe.route_flight.api;

import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.domain.RouteStatus;
import pt.isep.aisafe.route_flight.domain.RouteType;

import java.time.LocalDate;

public record RouteResponse(
        String routeId,
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
                null,
                null,
                null,
                route.getRouteRequirements() != null ? route.getRouteRequirements().getRequiredRange() : null,
                route.getRouteRequirements() != null ? route.getRouteRequirements().getRequiredCapacity() : null
        );
    }
}
