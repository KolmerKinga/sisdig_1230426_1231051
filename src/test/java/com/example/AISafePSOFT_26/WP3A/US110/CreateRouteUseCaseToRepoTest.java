package com.example.AISafePSOFT_26.WP3A.US110;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.domain.AirportLocation;
import com.example.AISafePSOFT_26.Airport.domain.AirportStatus;
import com.example.AISafePSOFT_26.Airport.domain.Facilities;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteRequirements;
import com.example.AISafePSOFT_26.Route.domain.RouteStatus;
import com.example.AISafePSOFT_26.Route.domain.RouteType;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import com.example.AISafePSOFT_26.exceptions.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateRouteUseCaseToRepoTest {
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
    void shouldCreateRouteWhenAirportsExistAndAreOperational() {
        Airport originAirport = airport("LIS", AirportStatus.OPERATIONAL);
        Airport destinationAirport = airport("OPO", AirportStatus.OPERATIONAL);
        RouteRequirements requirements = new RouteRequirements(3200.0, 160);

        when(routeRepository.findByRouteName("LIS-OPO"))
                .thenReturn(Optional.empty());
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(originAirport));
        when(airportRepository.findByIataCode("OPO"))
                .thenReturn(Optional.of(destinationAirport));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Route result = useCase.createRoute(
                "LIS-OPO",
                "LIS",
                "OPO",
                0.9,
                RouteType.DIRECT,
                requirements
        );

        ArgumentCaptor<Route> captor = ArgumentCaptor.forClass(Route.class);
        verify(routeRepository, times(1)).save(captor.capture());

        Route savedRoute = captor.getValue();
        assertEquals(savedRoute, result);
        assertEquals("LIS-OPO", savedRoute.getRouteName());
        assertEquals(originAirport, savedRoute.getOriginAirport());
        assertEquals(destinationAirport, savedRoute.getDestinationAirport());
        assertEquals(0.9, savedRoute.getEstimatedFlightTimeHours());
        assertEquals(RouteType.DIRECT, savedRoute.getType());
        assertEquals(RouteStatus.ACTIVE, savedRoute.getStatus());
        assertEquals(requirements, savedRoute.getRouteRequirements());
        assertNotNull(savedRoute.getRouteHistory());
        assertEquals(0, savedRoute.getRouteHistory().getRouteUsage());
    }

    @Test
    void shouldUseDirectRouteTypeWhenTypeIsNotProvided() {
        Airport originAirport = airport("LIS", AirportStatus.OPERATIONAL);
        Airport destinationAirport = airport("OPO", AirportStatus.OPERATIONAL);

        when(routeRepository.findByRouteName("LIS-OPO"))
                .thenReturn(Optional.empty());
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(originAirport));
        when(airportRepository.findByIataCode("OPO"))
                .thenReturn(Optional.of(destinationAirport));
        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Route result = useCase.createRoute(
                "LIS-OPO",
                "LIS",
                "OPO",
                0.9,
                null,
                new RouteRequirements(3200.0, 160)
        );

        assertEquals(RouteType.DIRECT, result.getType());
    }

    @Test
    void shouldThrowExceptionWhenRouteAlreadyExists() {
        when(routeRepository.findByRouteName("LIS-OPO"))
                .thenReturn(Optional.of(mock(Route.class)));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.createRoute(
                        "LIS-OPO",
                        "LIS",
                        "OPO",
                        0.9,
                        RouteType.DIRECT,
                        new RouteRequirements(3200.0, 160)
                )
        );

        assertEquals("Route already exists", exception.getMessage());
        verify(airportRepository, never()).findByIataCode(anyString());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenOriginAirportDoesNotExist() {
        when(routeRepository.findByRouteName("LIS-OPO"))
                .thenReturn(Optional.empty());
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.createRoute(
                        "LIS-OPO",
                        "LIS",
                        "OPO",
                        0.9,
                        RouteType.DIRECT,
                        new RouteRequirements(3200.0, 160)
                )
        );

        assertEquals("Origin airport does not exist", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenDestinationAirportIsNotOperational() {
        Airport originAirport = airport("LIS", AirportStatus.OPERATIONAL);
        Airport destinationAirport = airport("OPO", AirportStatus.CLOSED);

        when(routeRepository.findByRouteName("LIS-OPO"))
                .thenReturn(Optional.empty());
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(originAirport));
        when(airportRepository.findByIataCode("OPO"))
                .thenReturn(Optional.of(destinationAirport));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.createRoute(
                        "LIS-OPO",
                        "LIS",
                        "OPO",
                        0.9,
                        RouteType.DIRECT,
                        new RouteRequirements(3200.0, 160)
                )
        );

        assertEquals("Airport is not operational", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenOriginAndDestinationAreTheSame() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.createRoute(
                        "LIS-LIS",
                        "LIS",
                        "LIS",
                        0.9,
                        RouteType.DIRECT,
                        new RouteRequirements(3200.0, 160)
                )
        );

        assertEquals("Origin and destination airports must be different", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenFlightTimeIsNotPositive() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.createRoute(
                        "LIS-OPO",
                        "LIS",
                        "OPO",
                        0.0,
                        RouteType.DIRECT,
                        new RouteRequirements(3200.0, 160)
                )
        );

        assertEquals("Estimated flight time must be positive", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
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
