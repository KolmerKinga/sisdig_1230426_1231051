package com.example.AISafePSOFT_26.WP3B.US214;

import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteStatus;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListActiveRoutesUseCaseFromRepoTest {
    private RouteRepository routeRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        useCase = new RouteService(routeRepository, mock(FlightRepository.class));
    }

    @Test
    void shouldListActiveRoutesSortedByPopularity() {
        Route route = mock(Route.class);
        when(routeRepository.findByStatus(eq(RouteStatus.ACTIVE), any(Sort.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.listActiveRoutes("popularity");

        assertEquals(1, result.size());
        assertEquals(route, result.get(0));
        verify(routeRepository, times(1))
                .findByStatus(eq(RouteStatus.ACTIVE), any(Sort.class));
    }
}
