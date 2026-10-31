package pt.isep.aisafe.routeflight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.routeflight.domain.*;
import pt.isep.aisafe.routeflight.infrastructure.RouteRepository;
import pt.isep.aisafe.routeflight.shared.forwarding.PeerClient;

import java.time.LocalDate;

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

        RouteHistory history = new RouteHistory(LocalDate.now(), 0);

        Route route = new Route(
                requirements,
                RouteStatus.ACTIVE,
                type,
                history,
                estimatedTimeHours,
                distanceKm,
                originIata,
                destinationIata,
                routeName
        );

        return routeRepository.save(route);
    }

    @Transactional
    public Route activateRoute(Long routeId) {
        Route route = getRouteById(routeId);
        route.activateRoute();
        return routeRepository.save(route);
    }

    @Transactional
    public Route suspendRoute(Long routeId) {
        Route route = getRouteById(routeId);
        route.suspendRoute();
        return routeRepository.save(route);
    }

    @Transactional
    public Route retireRoute(Long routeId) {
        Route route = getRouteById(routeId);
        route.retireRoute();
        return routeRepository.save(route);
    }

    @Transactional(readOnly = true)
    public Route getRouteById(Long routeId) {
        return routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with ID: " + routeId));
    }

    private void validateAirportExists(String iataCode) {
        try {
            Boolean exists = peerClient.getForObject("http://airport-service/api/airports/" + iataCode + "/exists", Boolean.class);
            if (exists == null || !exists) {
                throw new IllegalArgumentException("Airport with IATA code '" + iataCode + "' was not found.");
            }
        } catch (Exception e) {
            // Log do erro e fallback caso o serviço externo esteja offline durante desenvolvimento
            System.err.println("Warning: Could not contact Airport_Service: " + e.getMessage());
        }
    }
}
