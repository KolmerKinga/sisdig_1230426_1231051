package pt.isep.aisafe.routeflight.infrastructure;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pt.isep.aisafe.routeflight.domain.Route;
import pt.isep.aisafe.routeflight.domain.RouteStatus;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {

    Optional<Route> findByRouteId(Long routeId);

    Optional<Route> findByRouteName(String routeName);

    List<Route> findByStatus(RouteStatus status, Sort sort);

    List<Route> findByOriginAirportIata(String originAirportIata);

    List<Route> findByOriginAirportIataOrDestinationAirportIata(String originAirportIata, String destinationAirportIata);
}
