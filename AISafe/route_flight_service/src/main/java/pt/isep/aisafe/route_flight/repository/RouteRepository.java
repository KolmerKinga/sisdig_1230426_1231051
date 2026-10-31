package pt.isep.aisafe.route_flight.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.domain.RouteStatus;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, String>, JpaSpecificationExecutor<Route> {

    Optional<Route> findByRouteId(String routeId);

    Optional<Route> findByRouteName(String routeName);

    List<Route> findByStatus(RouteStatus status, Sort sort);

    List<Route> findByOriginIata(String originIata);

    List<Route> findByOriginIataOrDestinationIata(String originIata, String destinationIata);
}
