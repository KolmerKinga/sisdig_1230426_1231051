package pt.isep.aisafe.routeflight.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.aisafe.routeflight.domain.Flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightId(Long flightId);

    List<Flight> findByAircraftRegistration(String aircraftRegistration);

    List<Flight> findByAircraftRegistrationAndScheduledDepartureBetween(
            String aircraftRegistration, LocalDateTime start, LocalDateTime end
    );

    boolean existsByAircraftRegistrationAndScheduledDepartureLessThanAndScheduledArrivalGreaterThan(
            String aircraftRegistration, LocalDateTime scheduledArrival, LocalDateTime scheduledDeparture
    );
}
