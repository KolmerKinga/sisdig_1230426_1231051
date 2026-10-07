package com.example.AISafePSOFT_26.WP3A.US114;

import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.exceptions.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchRoutesUseCaseFromRepoTest {
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
    @SuppressWarnings("unchecked")
    void shouldSearchRoutesMatchingOriginAndDestinationFilters() {
        Route route = mock(Route.class);
        when(routeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.searchRoutes("LIS", "OPO");

        assertEquals(1, result.size());
        assertEquals(route, result.get(0));
        verify(routeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSearchRoutesWithOnlyOriginFilter() {
        Route route = mock(Route.class);
        when(routeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.searchRoutes("LIS", null);

        assertEquals(List.of(route), result);
        verify(routeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSearchRoutesWithOnlyDestinationFilter() {
        Route route = mock(Route.class);
        when(routeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.searchRoutes(null, "OPO");

        assertEquals(List.of(route), result);
        verify(routeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSearchAllRoutesWhenNoFiltersAreProvided() {
        Route route = mock(Route.class);
        when(routeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(route));

        List<Route> result = useCase.searchRoutes(null, null);

        assertEquals(List.of(route), result);
        verify(routeRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenOriginIataCodeIsInvalid() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.searchRoutes("lis", "OPO")
        );

        assertEquals("IATA code must have 3 uppercase letters", exception.getMessage());
        verify(routeRepository, never()).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionWhenDestinationIataCodeIsInvalid() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.searchRoutes("LIS", "OP")
        );

        assertEquals("IATA code must have 3 uppercase letters", exception.getMessage());
        verify(routeRepository, never()).findAll(any(Specification.class));
    }
}
