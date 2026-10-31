package pt.isep.aisafe.route_flight.application;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.domain.RouteStatus;
import pt.isep.aisafe.route_flight.repository.RouteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RouteSearchService {

    private final RouteRepository routeRepository;

    public RouteSearchService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAllRoutes() {
        return routeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Route> findByRouteName(String name) {
        return routeRepository.findByRouteName(name);
    }

    @Transactional(readOnly = true)
    public List<Route> findByStatus(RouteStatus status) {
        return routeRepository.findByStatus(status, Sort.by("routeName").ascending());
    }

    @Transactional(readOnly = true)
    public List<Route> findByAirport(String iataCode) {
        return routeRepository.findByOriginIataOrDestinationIata(iataCode, iataCode);
    }
}
