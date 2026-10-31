package pt.isep.aisafe.route_flight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.*;
import pt.isep.aisafe.route_flight.repository.RouteRepository;
import pt.isep.aisafe.route_flight.shared.forwarding.PeerClient;

import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final PeerClient peerClient;

    public RouteService(RouteRepository routeRepository, PeerClient peerClient) {
        this.routeRepository = routeRepository;
        this.peerClient = peerClient;
    }

    @Transactional
    public Route createRoute(String routeName, RouteType type, String originIata, String destinationIata,
                             Double estimatedTimeHours, Double distanceKm, RouteRequirements requirements) {

        if (routeRepository.findByRouteName(routeName).isPresent()) {
            throw new IllegalArgumentException("Route with name '" + routeName + "' already exists.");
        }

        validateAirportExists(originIata);
        validateAirportExists(destinationIata);

        Route route = new Route(
                routeName,
                type,
                originIata,
                destinationIata,
                estimatedTimeHours,
                distanceKm,
                requirements
        );

        return routeRepository.save(route);
    }

    @Transactional
    public Route activateRoute(String routeId) {
        Route route = getRouteById(routeId);
        route.activateRoute();
        return routeRepository.save(route);
    }

    @Transactional
    public Route suspendRoute(String routeId) {
        Route route = getRouteById(routeId);
        route.suspendRoute();
        return routeRepository.save(route);
    }

    @Transactional
    public Route retireRoute(String routeId) {
        Route route = getRouteById(routeId);
        route.retireRoute();
        return routeRepository.save(route);
    }

    @Transactional(readOnly = true)
    public Route getRouteById(String routeId) {
        return routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with ID: " + routeId));
    }

    @Transactional(readOnly = true)
    public Optional<Route> findById(String routeId) {
        return routeRepository.findByRouteId(routeId);
    }

    private void validateAirportExists(String iataCode) {
        try {
            Boolean exists = peerClient.getForObject("http://localhost:8081/api/airports/" + iataCode + "/exists", Boolean.class);
            if (exists == null || !exists) {
                throw new IllegalArgumentException("Airport with IATA code '" + iataCode + "' was not found.");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not contact Airport_Service: " + e.getMessage());
        }
    }
}
