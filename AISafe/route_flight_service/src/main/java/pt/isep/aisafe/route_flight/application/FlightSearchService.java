package pt.isep.aisafe.route_flight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.repository.FlightRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightSearchService {

    private final FlightRepository flightRepository;

    public FlightSearchService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<Flight> findAllFlights() {
        return flightRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Flight> findByAircraft(String aircraftRegistration) {
        return flightRepository.findByAircraftRegistration(aircraftRegistration);
    }

    @Transactional(readOnly = true)
    public List<Flight> findByAircraftAndDateRange(String aircraftRegistration, LocalDateTime start, LocalDateTime end) {
        return flightRepository.findByAircraftRegistrationAndScheduledDepartureBetween(aircraftRegistration, start, end);
    }
}
