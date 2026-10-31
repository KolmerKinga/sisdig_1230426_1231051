package pt.isep.aisafe.routeflight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.routeflight.domain.Flight;
import pt.isep.aisafe.routeflight.domain.Route;
import pt.isep.aisafe.routeflight.infrastructure.FlightRepository;
import pt.isep.aisafe.routeflight.infrastructure.RouteRepository;
import pt.isep.aisafe.routeflight.shared.forwarding.PeerClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final PeerClient peerClient;

    public FlightService(FlightRepository flightRepository, RouteRepository routeRepository, PeerClient peerClient) {
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.peerClient = peerClient;
    }

    @Transactional
    public Flight scheduleFlight(Long routeId, String aircraftRegistration, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with ID: " + routeId));

        if (!route.isOperational()) {
            throw new IllegalStateException("Cannot schedule flight on an inactive or archived route.");
        }

        validateAircraftAvailable(aircraftRegistration);

        boolean hasOverlap = flightRepository.existsByAircraftRegistrationAndScheduledDepartureLessThanAndScheduledArrivalGreaterThan(
                aircraftRegistration, scheduledArrival, scheduledDeparture
        );

        if (hasOverlap) {
            throw new IllegalStateException("Aircraft '" + aircraftRegistration + "' already has a flight scheduled in this timeframe.");
        }

        Flight flight = new Flight(route, aircraftRegistration, scheduledDeparture, scheduledArrival);
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight startFlight(Long flightId) {
        Flight flight = getFlightById(flightId);
        flight.startFlight();
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight completeFlight(Long flightId) {
        Flight flight = getFlightById(flightId);
        flight.completeFlight();

        if (flight.getRoute().getRouteHistory() != null) {
            flight.getRoute().getRouteHistory().incrementUsage();
        }

        Double flightHours = flight.getFlightHours();
        if (flightHours != null && flightHours > 0) {
            updateAircraftMetricsInAircraftService(
                    flight.getAircraftRegistration(),
                    flightHours,
                    flight.getRoute().getDistanceKm()
            );
        }

        return flightRepository.save(flight);
    }

    @Transactional
    public Flight cancelFlight(Long flightId) {
        Flight flight = getFlightById(flightId);
        flight.cancelFlight();
        return flightRepository.save(flight);
    }

    @Transactional(readOnly = true)
    public Flight getFlightById(Long flightId) {
        return flightRepository.findByFlightId(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + flightId));
    }

    private void validateAircraftAvailable(String aircraftRegistration) {
        try {
            Map<?, ?> aircraftDto = peerClient.getForObject("http://aircraft-service/api/aircraft/" + aircraftRegistration, Map.class);
            if (aircraftDto != null) {
                Object status = aircraftDto.get("status");
                if (status != null && !"AVAILABLE".equals(status.toString())) {
                    throw new IllegalStateException("Aircraft '" + aircraftRegistration + "' is not AVAILABLE.");
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not contact Aircraft_Service: " + e.getMessage());
        }
    }

    private void updateAircraftMetricsInAircraftService(String registration, Double flightHours, Double distanceKm) {
        try {
            Map<String, Object> body = Map.of("flightHours", flightHours, "distanceKm", distanceKm);
            peerClient.postForObject("http://aircraft-service/api/aircraft/" + registration + "/update-metrics", body, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to update aircraft metrics: " + e.getMessage());
        }
    }
}
