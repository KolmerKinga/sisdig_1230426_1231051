package pt.isep.aisafe.routeflight.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.aisafe.routeflight.api.dto.CreateFlightRequest;
import pt.isep.aisafe.routeflight.api.dto.FlightResponse;
import pt.isep.aisafe.routeflight.application.FlightSearchService;
import pt.isep.aisafe.routeflight.application.FlightService;
import pt.isep.aisafe.routeflight.application.FlightUtilizationReportService;
import pt.isep.aisafe.routeflight.domain.Flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;
    private final FlightSearchService flightSearchService;
    private final FlightUtilizationReportService reportService;

    public FlightController(FlightService flightService,
                            FlightSearchService flightSearchService,
                            FlightUtilizationReportService reportService) {
        this.flightService = flightService;
        this.flightSearchService = flightSearchService;
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<FlightResponse> scheduleFlight(@Valid @RequestBody CreateFlightRequest request) {
        Flight flight = flightService.scheduleFlight(
                request.routeId(),
                request.aircraftRegistration(),
                request.scheduledDeparture(),
                request.scheduledArrival()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(FlightResponse.fromDomain(flight));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getFlights(
            @RequestParam(required = false) String aircraftRegistration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Flight> flights;
        if (aircraftRegistration != null && start != null && end != null) {
            flights = flightSearchService.findByAircraftAndDateRange(aircraftRegistration, start, end);
        } else if (aircraftRegistration != null) {
            flights = flightSearchService.findByAircraft(aircraftRegistration);
        } else {
            flights = flightSearchService.findAllFlights();
        }

        List<FlightResponse> response = flights.stream()
                .map(FlightResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<FlightResponse> startFlight(@PathVariable Long id) {
        Flight flight = flightService.startFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<FlightResponse> completeFlight(@PathVariable Long id) {
        Flight flight = flightService.completeFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FlightResponse> cancelFlight(@PathVariable Long id) {
        Flight flight = flightService.cancelFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @GetMapping("/aircraft/{registration}/total-hours")
    public ResponseEntity> getAircraftTotalHours(@PathVariable String registration) {
        Double totalHours = reportService.calculateTotalFlightHoursForAircraft(registration);
        long completedFlights = reportService.countCompletedFlightsForAircraft(registration);

        Map<String, Object> response = Map.of(
                "aircraftRegistration", registration,
                "totalFlightHours", totalHours,
                "completedFlightsCount", completedFlights
        );

        return ResponseEntity.ok(response);
    }
}package pt.isep.aisafe.routeflight.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import pt.isep.aisafe.routeflight.api.dto.CreateFlightRequest;
import pt.isep.aisafe.routeflight.api.dto.FlightResponse;
import pt.isep.aisafe.routeflight.application.FlightSearchService;
import pt.isep.aisafe.routeflight.application.FlightService;
import pt.isep.aisafe.routeflight.application.FlightUtilizationReportService;
import pt.isep.aisafe.routeflight.domain.Flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;
    private final FlightSearchService flightSearchService;
    private final FlightUtilizationReportService reportService;

    public FlightController(FlightService flightService,
                            FlightSearchService flightSearchService,
                            FlightUtilizationReportService reportService) {
        this.flightService = flightService;
        this.flightSearchService = flightSearchService;
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<FlightResponse> scheduleFlight(@Valid @RequestBody CreateFlightRequest request) {
        Flight flight = flightService.scheduleFlight(
                request.routeId(),
                request.aircraftRegistration(),
                request.scheduledDeparture(),
                request.scheduledArrival()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(FlightResponse.fromDomain(flight));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getFlights(
            @RequestParam(required = false) String aircraftRegistration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Flight> flights;
        if (aircraftRegistration != null && start != null && end != null) {
            flights = flightSearchService.findByAircraftAndDateRange(aircraftRegistration, start, end);
        } else if (aircraftRegistration != null) {
            flights = flightSearchService.findByAircraft(aircraftRegistration);
        } else {
            flights = flightSearchService.findAllFlights();
        }

        List<FlightResponse> response = flights.stream()
                .map(FlightResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<FlightResponse> startFlight(@PathVariable Long id) {
        Flight flight = flightService.startFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<FlightResponse> completeFlight(@PathVariable Long id) {
        Flight flight = flightService.completeFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<FlightResponse> cancelFlight(@PathVariable Long id) {
        Flight flight = flightService.cancelFlight(id);
        return ResponseEntity.ok(FlightResponse.fromDomain(flight));
    }

    @GetMapping("/aircraft/{registration}/total-hours")
    public ResponseEntity> getAircraftTotalHours(@PathVariable String registration) {
        Double totalHours = reportService.calculateTotalFlightHoursForAircraft(registration);
        long completedFlights = reportService.countCompletedFlightsForAircraft(registration);

        Map<String, Object> response = Map.of(
                "aircraftRegistration", registration,
                "totalFlightHours", totalHours,
                "completedFlightsCount", completedFlights
        );

        return ResponseEntity.ok(response);
    }
}
