package com.example.AISafePSOFT_26.WP3A.US113;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.exceptions.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewRoutesUseCaseFromRepoTest {
    private RouteRepository routeRepository;
    private AirportRepository airportRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        airportRepository = mock(AirportRepository.class);
        useCase = new RouteService(routeRepository, airportRepository);
    }

    @Test
    void shouldReturnRoutesFromAirportWhenAirportExists() {
        Airport airport = mock(Airport.class);
        Route route = mock(Route.class);

        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(airport));
        when(routeRepository.findByOriginAirport_IataCode("LIS"))
                .thenReturn(List.of(route));

        List<Route> result = useCase.getRoutesFromAirport("LIS");

        assertEquals(1, result.size());
        assertEquals(route, result.get(0));
        verify(airportRepository, times(1)).findByIataCode("LIS");
        verify(routeRepository, times(1)).findByOriginAirport_IataCode("LIS");
    }

    @Test
    void shouldThrowExceptionWhenAirportDoesNotExist() {
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.getRoutesFromAirport("LIS")
        );

        assertEquals("Airport does not exist", exception.getMessage());
        verify(routeRepository, never()).findByOriginAirport_IataCode(anyString());
    }

    @Test
    void shouldReturnRouteDetailsWhenRouteExists() {
        Route route = mock(Route.class);
        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));

        Route result = useCase.getRouteDetails(1L);

        assertEquals(route, result);
        verify(routeRepository, times(1)).findByRouteId(1L);
    }

    @Test
    void shouldThrowExceptionWhenRouteDoesNotExist() {
        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.getRouteDetails(1L)
        );

        assertEquals("Route does not exist", exception.getMessage());
    }
}
