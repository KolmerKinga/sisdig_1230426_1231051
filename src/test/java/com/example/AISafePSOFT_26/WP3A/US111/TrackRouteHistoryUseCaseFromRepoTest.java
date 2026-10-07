package com.example.AISafePSOFT_26.WP3A.US111;

import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteHistory;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.exceptions.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackRouteHistoryUseCaseFromRepoTest {
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
    void shouldReturnRouteHistoryWhenRouteExists() {
        Route route = mock(Route.class);
        RouteHistory history = new RouteHistory(LocalDate.of(2026, 1, 1), 12);

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));
        when(route.getRouteHistory()).thenReturn(history);

        RouteHistory result = useCase.getRouteHistory(1L);

        assertEquals(history, result);
        assertEquals(LocalDate.of(2026, 1, 1), result.getRouteBegin());
        assertNull(result.getRouteFinish());
        assertEquals(12, result.getRouteUsage());
        verify(routeRepository, times(1)).findByRouteId(1L);
        verify(route, times(1)).getRouteHistory();
    }

    @Test
    void shouldThrowExceptionWhenRouteDoesNotExist() {
        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.getRouteHistory(1L)
        );

        assertEquals("Route does not exist", exception.getMessage());
    }
}
