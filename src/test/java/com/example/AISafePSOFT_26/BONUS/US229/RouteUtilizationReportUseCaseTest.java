package com.example.AISafePSOFT_26.BONUS.US229;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Flight.application.FlightUtilizationReportService;
import com.example.AISafePSOFT_26.Flight.domain.Flight;
import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.domain.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteUtilizationReportUseCaseTest {

    private FlightRepository flightRepository;
    private FlightUtilizationReportService service;

    @BeforeEach
    void setUp() {
        flightRepository = mock(FlightRepository.class);
        service = new FlightUtilizationReportService(flightRepository);
    }

    @Test
    void shouldReturnEmptyReportWhenThereAreNoFlights() {
        when(flightRepository.findAll()).thenReturn(List.of());

        List<FlightUtilizationReportService.RouteUtilizationReport> result =
                service.getRouteUtilizationReport();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCountFlightsPerRouteAndSortByMostFlown() {
        Route lisOpo = route(1L, "LIS-OPO", "LIS", "OPO");
        Route lisMad = route(2L, "LIS-MAD", "LIS", "MAD");
        Aircraft aircraft = mock(Aircraft.class);

        when(flightRepository.findAll()).thenReturn(List.of(
                flight(lisOpo, aircraft),
                flight(lisMad, aircraft),
                flight(lisOpo, aircraft)
        ));

        List<FlightUtilizationReportService.RouteUtilizationReport> result =
                service.getRouteUtilizationReport();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).routeId());
        assertEquals("LIS-OPO", result.get(0).routeName());
        assertEquals("LIS", result.get(0).originIata());
        assertEquals("OPO", result.get(0).destinationIata());
        assertEquals(2L, result.get(0).flightCount());
        assertEquals(2L, result.get(1).routeId());
        assertEquals(1L, result.get(1).flightCount());
    }

    private Flight flight(Route route, Aircraft aircraft) {
        return new Flight(route, aircraft,
                LocalDateTime.of(2026, 6, 21, 10, 0),
                LocalDateTime.of(2026, 6, 21, 11, 0));
    }

    private Route route(Long routeId, String routeName, String originIata,
            String destinationIata) {
        Airport origin = mock(Airport.class);
        Airport destination = mock(Airport.class);
        Route route = mock(Route.class);

        when(origin.getIataCode()).thenReturn(originIata);
        when(destination.getIataCode()).thenReturn(destinationIata);
        when(route.getRouteId()).thenReturn(routeId);
        when(route.getRouteName()).thenReturn(routeName);
        when(route.getOriginAirport()).thenReturn(origin);
        when(route.getDestinationAirport()).thenReturn(destination);

        return route;
    }
}
