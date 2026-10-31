package pt.isep.aisafe.route_flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.aisafe.route_flight.domain.Flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, String> {

    Optional<Flight> findByFlightId(String flightId);

    List<Flight> findByAircraftRegistration(String aircraftRegistration);

    List<Flight> findByAircraftRegistrationAndScheduledDepartureBetween(
            String aircraftRegistration, LocalDateTime start, LocalDateTime end
    );

    boolean existsByAircraftRegistrationAndScheduledDepartureLessThanAndScheduledArrivalGreaterThan(
            String aircraftRegistration, LocalDateTime scheduledArrival, LocalDateTime scheduledDeparture
    );
}
