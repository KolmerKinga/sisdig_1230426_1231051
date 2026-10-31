package pt.isep.aisafe.route_flight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.repository.RouteRepository;

import java.util.List;

@Service
public class RouteExportService {

    private final RouteRepository routeRepository;

    public RouteExportService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public String exportRoutesToCsv() {
        List<Route> routes = routeRepository.findAll();
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Name,Type,Status,OriginIATA,DestinationIATA,DistanceKM,EstTimeHours\n");

        for (Route route : routes) {
            csvBuilder.append(route.getRouteId()).append(",")
                    .append(route.getRouteName()).append(",")
                    .append(route.getType()).append(",")
                    .append(route.getStatus()).append(",")
                    .append(route.getOriginAirportIata()).append(",")
                    .append(route.getDestinationAirportIata()).append(",")
                    .append(route.getDistanceKm()).append(",")
                    .append(route.getEstimatedFlightTimeHours()).append("\n");
        }

        return csvBuilder.toString();
    }
}
