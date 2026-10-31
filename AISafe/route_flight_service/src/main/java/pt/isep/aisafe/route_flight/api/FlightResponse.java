package pt.isep.aisafe.route_flight.api;

import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.domain.FlightStatus;

import java.time.LocalDateTime;

public record FlightResponse(
        String flightId,
        String routeId,
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
                flight.getRouteId(),
                null,
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
