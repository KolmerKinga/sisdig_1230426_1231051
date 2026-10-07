package com.example.AISafePSOFT_26.WP3A.US112;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.domain.AirportLocation;
import com.example.AISafePSOFT_26.Airport.domain.AirportStatus;
import com.example.AISafePSOFT_26.Airport.domain.Facilities;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteHistory;
import com.example.AISafePSOFT_26.Route.domain.RouteRequirements;
import com.example.AISafePSOFT_26.Route.domain.RouteStatus;
import com.example.AISafePSOFT_26.Route.domain.RouteType;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.exceptions.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateRouteUseCaseToRepoTest {
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
    void shouldUpdateRouteDestinationFlightTimeAndStatus() {
        Airport originAirport = airport("LIS", AirportStatus.OPERATIONAL);
        Airport oldDestinationAirport = airport("OPO", AirportStatus.OPERATIONAL);
        Airport newDestinationAirport = airport("MAD", AirportStatus.OPERATIONAL);
        Route route = route(originAirport, oldDestinationAirport);

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));
        when(airportRepository.findByIataCode("MAD"))
                .thenReturn(Optional.of(newDestinationAirport));
        when(routeRepository.save(route)).thenReturn(route);

        Route result = useCase.updateRoute(
                1L,
                "MAD",
                1.4,
                RouteStatus.INACTIVE
        );

        assertEquals(route, result);
        assertEquals(newDestinationAirport, result.getDestinationAirport());
        assertEquals(1.4, result.getEstimatedFlightTimeHours());
        assertEquals(RouteStatus.INACTIVE, result.getStatus());
        verify(routeRepository, times(1)).save(route);
    }

    @Test
    void shouldArchiveRouteAndSetFinishDate() {
        Route route = route(
                airport("LIS", AirportStatus.OPERATIONAL),
                airport("OPO", AirportStatus.OPERATIONAL)
        );

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));
        when(routeRepository.save(route)).thenReturn(route);

        Route result = useCase.updateRoute(1L, null, null, RouteStatus.ARCHIVED);

        assertEquals(RouteStatus.ARCHIVED, result.getStatus());
        assertEquals(LocalDate.now(), result.getRouteHistory().getRouteFinish());
        verify(routeRepository, times(1)).save(route);
    }

    @Test
    void shouldThrowExceptionWhenRouteDoesNotExist() {
        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.updateRoute(1L, "MAD", 1.4, RouteStatus.INACTIVE)
        );

        assertEquals("Route does not exist", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenNewDestinationDoesNotExist() {
        Route route = route(
                airport("LIS", AirportStatus.OPERATIONAL),
                airport("OPO", AirportStatus.OPERATIONAL)
        );

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));
        when(airportRepository.findByIataCode("MAD"))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.updateRoute(1L, "MAD", null, null)
        );

        assertEquals("Destination airport does not exist", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenNewDestinationIsOriginAirport() {
        Route route = route(
                airport("LIS", AirportStatus.OPERATIONAL),
                airport("OPO", AirportStatus.OPERATIONAL)
        );

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.updateRoute(1L, "LIS", null, null)
        );

        assertEquals("Origin and destination airports must be different", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenFlightTimeIsNotPositive() {
        Route route = route(
                airport("LIS", AirportStatus.OPERATIONAL),
                airport("OPO", AirportStatus.OPERATIONAL)
        );

        when(routeRepository.findByRouteId(1L))
                .thenReturn(Optional.of(route));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.updateRoute(1L, null, 0.0, null)
        );

        assertEquals("Estimated flight time must be positive", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    private Route route(Airport originAirport, Airport destinationAirport) {
        return new Route(
                new RouteRequirements(3200.0, 160),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2026, 1, 1), 0),
                0.9,
                originAirport,
                destinationAirport,
                "LIS-OPO"
        );
    }

    private Airport airport(String iataCode, AirportStatus status) {
        return new Airport(
                iataCode,
                "International",
                iataCode + " Airport",
                status,
                new AirportLocation(iataCode, -9.1359, 38.7742,
                        iataCode, "Europe/Lisbon", "Portugal"),
                new Facilities(2, List.of("fuel"), 48),
                0.0,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
