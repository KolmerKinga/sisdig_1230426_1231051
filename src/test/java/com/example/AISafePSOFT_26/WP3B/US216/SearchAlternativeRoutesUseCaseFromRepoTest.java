package com.example.AISafePSOFT_26.WP3B.US216;

import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchAlternativeRoutesUseCaseFromRepoTest {
    private RouteRepository routeRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        useCase = new RouteService(routeRepository, mock(FlightRepository.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSearchAlternativeRoutesBetweenAirports() {
        Route route = mock(Route.class);
        when(routeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.searchAlternativeRoutes("LIS", "OPO");

        assertEquals(1, result.size());
        assertEquals(route, result.get(0));
        verify(routeRepository, times(1)).findAll(any(Specification.class));
    }
}
