package com.example.AISafePSOFT_26.WP3B.US212;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.domain.AircraftAvailability;
import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftModel;
import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftSpecs;
import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.domain.AirportStatus;
import com.example.AISafePSOFT_26.Flight.domain.Flight;
import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.*;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ScheduleFlightUseCaseToRepoTest {
    private RouteRepository routeRepository;
    private FlightRepository flightRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        flightRepository = mock(FlightRepository.class);
        useCase = new RouteService(routeRepository, flightRepository);
    }

    @Test
    void shouldScheduleFlightWhenAircraftAndRouteAreAvailableAndCompatible() {
        Airport origin = operationalAirport("LIS");
        Airport destination = operationalAirport("OPO");
        Route route = new Route(
                new RouteRequirements(850.0, 120),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2026, 1, 1), 0),
                1.0,
                320.0,
                origin,
                destination,
                "LIS-OPO"
        );
        Aircraft aircraft = compatibleAvailableAircraft();
        LocalDateTime departure = LocalDateTime.of(2026, 6, 10, 9, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 6, 10, 10, 0);

        when(routeRepository.findByRouteId(1L)).thenReturn(Optional.of(route));
        when(flightRepository.existsByAircraft_RegistrationNumberAndScheduledDepartureLessThanAndScheduledArrivalGreaterThan(
                "CS-TST", arrival, departure)).thenReturn(false);
        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Flight result = useCase.scheduleFlight(1L, aircraft, departure, arrival);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository, times(1)).save(captor.capture());
        verify(routeRepository, times(1)).save(route);

        assertEquals(route, result.getRoute());
        assertEquals(aircraft, result.getAircraft());
        assertEquals(departure, result.getScheduledDeparture());
        assertEquals(arrival, result.getScheduledArrival());
        assertEquals(1, route.getRouteHistory().getRouteUsage());
        assertEquals(route, captor.getValue().getRoute());
    }

    private Airport operationalAirport(String iataCode) {
        Airport airport = mock(Airport.class);
        when(airport.getIataCode()).thenReturn(iataCode);
        when(airport.getStatus()).thenReturn(AirportStatus.OPERATIONAL);
        return airport;
    }

    private Aircraft compatibleAvailableAircraft() {
        Aircraft aircraft = mock(Aircraft.class);
        AircraftModel model = mock(AircraftModel.class);

        when(aircraft.getRegistrationNumber()).thenReturn("CS-TST");
        when(aircraft.getStatus()).thenReturn(AircraftAvailability.AVAILABLE);
        when(aircraft.getMeanRange()).thenReturn(null);
        when(aircraft.getModel()).thenReturn(model);
        when(model.getAircraftModelSpecs())
                .thenReturn(new AircraftSpecs(24210.0, 1200.0, 840.0, 180));
        return aircraft;
    }
}
