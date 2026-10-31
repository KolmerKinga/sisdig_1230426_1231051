package pt.isep.aisafe.route_flight.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.route_flight.domain.Flight;
import pt.isep.aisafe.route_flight.domain.FlightStatus;
import pt.isep.aisafe.route_flight.repository.FlightRepository;

import java.util.List;

@Service
public class FlightUtilizationReportService {

    private final FlightRepository flightRepository;

    public FlightUtilizationReportService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public Double calculateTotalFlightHoursForAircraft(String aircraftRegistration) {
        List<Flight> flights = flightRepository.findByAircraftRegistration(aircraftRegistration);

        return flights.stream()
                .filter(f -> f.getFlightStatus() == FlightStatus.COMPLETED)
                .mapToDouble(f -> f.getFlightHours() != null ? f.getFlightHours() : 0.0)
                .sum();
    }

    @Transactional(readOnly = true)
    public long countCompletedFlightsForAircraft(String aircraftRegistration) {
        return flightRepository.findByAircraftRegistration(aircraftRegistration).stream()
                .filter(f -> f.getFlightStatus() == FlightStatus.COMPLETED)
                .count();
    }
}
