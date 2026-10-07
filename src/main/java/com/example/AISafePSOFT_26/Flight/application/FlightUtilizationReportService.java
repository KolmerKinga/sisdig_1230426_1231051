package com.example.AISafePSOFT_26.Flight.application;

import com.example.AISafePSOFT_26.Flight.domain.Flight;
import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.domain.Route;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FlightUtilizationReportService {
    private final FlightRepository flightRepository;

    public FlightUtilizationReportService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<RouteUtilizationReport> getRouteUtilizationReport() {
        return flightRepository.findAll()
                .stream()
                .filter(flight -> flight.getRoute() != null)
                .collect(Collectors.groupingBy(Flight::getRoute, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> toReport(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparing(RouteUtilizationReport::flightCount).reversed()
                        .thenComparing(RouteUtilizationReport::routeName))
                .toList();
    }

    private RouteUtilizationReport toReport(Route route, Long flightCount) {
        return new RouteUtilizationReport(route.getRouteId(), route.getRouteName(),
                route.getOriginAirport().getIataCode(),
                route.getDestinationAirport().getIataCode(),
                flightCount);
    }

    public record RouteUtilizationReport(Long routeId, String routeName,
            String originIata, String destinationIata, Long flightCount) {
    }
}
