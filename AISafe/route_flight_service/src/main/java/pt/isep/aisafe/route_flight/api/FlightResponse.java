package pt.isep.aisafe.routeflight.api.dto;

import pt.isep.aisafe.routeflight.domain.Flight;
import pt.isep.aisafe.routeflight.domain.FlightStatus;

import java.time.LocalDateTime;

public record FlightResponse(
        Long flightId,
        Long routeId,
        String routeName,
        String aircraftRegistration,
        FlightStatus status,
        LocalDateTime scheduledDeparture,
        LocalDateTime scheduledArrival,
        LocalDateTime timeOfDeparture,
        LocalDateTime timeOfArrival,
        Double flightHours
) {
    public static FlightResponse fromDomain(Flight flight) {
        return new FlightResponse(
                flight.getFlightId(),
                flight.getRoute() != null ? flight.getRoute().getRouteId() : null,
                flight.getRoute() != null ? flight.getRoute().getRouteName() : null,
                flight.getAircraftRegistration(),
                flight.getFlightStatus(),
                flight.getScheduledDeparture(),
                flight.getScheduledArrival(),
                flight.getTimeOfDeparture(),
                flight.getTimeOfArrival(),
                flight.getFlightHours()
        );
    }
}
