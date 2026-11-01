package pt.isep.aisafe.route_flight.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import pt.isep.aisafe.route_flight.RouteFlightApplication;
import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.domain.FlightStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = RouteFlightApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void shouldSaveAndFindFlightById() {
        LocalDateTime departure = LocalDateTime.of(2026, 11, 2, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 11, 2, 12, 0);

        Flight flight = new Flight("route-100", "CS-ABC", departure, arrival);
        Flight savedFlight = flightRepository.save(flight);

        assertNotNull(savedFlight.getFlightId());

        Optional<Flight> found = flightRepository.findByFlightId(savedFlight.getFlightId());
        assertTrue(found.isPresent());
        assertEquals("CS-ABC", found.get().getAircraftRegistration());
        assertEquals(FlightStatus.SCHEDULED, found.get().getStatus());
    }
}
