package com.example.AISafePSOFT_26.WP3B.US215;

import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CalculateTotalRouteDistanceUseCaseFromRepoTest {
    private RouteRepository routeRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        useCase = new RouteService(routeRepository, mock(FlightRepository.class));
    }

    @Test
    void shouldCalculateTotalDistanceForAllRoutes() {
        Route first = mock(Route.class);
        Route second = mock(Route.class);
        when(first.getDistanceKm()).thenReturn(320.0);
        when(second.getDistanceKm()).thenReturn(620.0);
        when(routeRepository.findAll()).thenReturn(List.of(first, second));

        Double result = useCase.calculateTotalRouteDistance();

        assertEquals(940.0, result);
        verify(routeRepository, times(1)).findAll();
    }
}
