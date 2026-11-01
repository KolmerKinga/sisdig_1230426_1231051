package pt.isep.aisafe.route_flight.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.repository.FlightRepository;
import pt.isep.aisafe.route_flight.repository.RouteRepository;
import pt.isep.aisafe.route_flight.domain.FlightStatus;
import pt.isep.aisafe.route_flight.domain.Route;
import pt.isep.aisafe.route_flight.domain.RouteStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldScheduleFlightSuccessfully() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        Route mockRoute = mock(Route.class);
        when(mockRoute.getStatus()).thenReturn(RouteStatus.ACTIVE);
        when(routeRepository.findByRouteId("route-123")).thenReturn(Optional.of(mockRoute));

        Flight flight = new Flight("route-123", "CS-ABC", departure, arrival);
        when(flightRepository.existsByAircraftRegistrationAndScheduledDepartureLessThanAndScheduledArrivalGreaterThan(any(), any(), any())).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight scheduled = flightService.scheduleFlight("route-123", "CS-ABC", departure, arrival);

        assertNotNull(scheduled);
        assertEquals("CS-ABC", scheduled.getAircraftRegistration());
        assertEquals(FlightStatus.SCHEDULED, scheduled.getStatus());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void shouldStartFlightSuccessfully() {
        LocalDateTime departure = LocalDateTime.now();
        LocalDateTime arrival = departure.plusHours(1);
        Flight flight = new Flight("route-123", "CS-ABC", departure, arrival);

        when(flightRepository.findByFlightId(flight.getFlightId())).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight startedFlight = flightService.startFlight(flight.getFlightId());

        assertEquals(FlightStatus.IN_AIR, startedFlight.getStatus());
    }
}
