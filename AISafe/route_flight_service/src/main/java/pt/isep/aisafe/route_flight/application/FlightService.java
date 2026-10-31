package pt.isep.aisafe.route_flight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.repository.FlightRepository;
import pt.isep.aisafe.route_flight.repository.RouteRepository;
import pt.isep.aisafe.route_flight.shared.forwarding.PeerClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    public Flight scheduleFlight(String routeId, String aircraftRegistration, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
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
    public Flight startFlight(String flightId) {
        Flight flight = getFlightById(flightId);
        flight.startFlight();
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight completeFlight(String flightId) {
        Flight flight = getFlightById(flightId);
        flight.completeFlight();

        Optional<Route> routeOpt = routeRepository.findByRouteId(flight.getRouteId());
        Double distanceKm = routeOpt.map(Route::getDistanceKm).orElse(0.0);

        Double flightHours = flight.getFlightHours();
        if (flightHours != null && flightHours > 0) {
            updateAircraftMetricsInAircraftService(
                    flight.getAircraftRegistration(),
                    flightHours,
                    distanceKm
            );
        }

        return flightRepository.save(flight);
    }

    @Transactional
    public Flight cancelFlight(String flightId) {
        Flight flight = getFlightById(flightId);
        flight.cancelFlight();
        return flightRepository.save(flight);
    }

    @Transactional(readOnly = true)
    public Flight getFlightById(String flightId) {
        return flightRepository.findByFlightId(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + flightId));
    }

    @Transactional(readOnly = true)
    public Optional<Flight> findById(String flightId) {
        return flightRepository.findByFlightId(flightId);
    }

    private void validateAircraftAvailable(String aircraftRegistration) {
        try {
            Map<?, ?> aircraftDto = peerClient.getForObject("http://localhost:8081/api/aircraft/" + aircraftRegistration, Map.class);
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
            peerClient.postForObject("http://localhost:8081/api/aircraft/" + registration + "/update-metrics", body, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to update aircraft metrics: " + e.getMessage());
        }
    }
}
