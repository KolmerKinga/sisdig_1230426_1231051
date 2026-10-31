package pt.isep.aisafe.routeflight.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "company_flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @Version
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "aircraft_registration", nullable = false)
    private String aircraftRegistration;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_status", nullable = false)
    private FlightStatus flightStatus;

    @Column(name = "scheduled_departure", nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival", nullable = false)
    private LocalDateTime scheduledArrival;

    @Column(name = "time_of_departure")
    private LocalDateTime timeOfDeparture;

    @Column(name = "time_of_arrival")
    private LocalDateTime timeOfArrival;

    protected Flight() {}

    public Flight(Route route, String aircraftRegistration, LocalDateTime scheduledDeparture,
                  LocalDateTime scheduledArrival) {
        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        if (aircraftRegistration == null || aircraftRegistration.isBlank()) {
            throw new IllegalArgumentException("Aircraft registration cannot be empty");
        }
        if (scheduledDeparture == null || scheduledArrival == null) {
            throw new IllegalArgumentException("Scheduled departure and arrival cannot be null");
        }
        if (scheduledArrival.isBefore(scheduledDeparture)) {
            throw new IllegalArgumentException("Scheduled arrival must be after scheduled departure");
        }

        this.route = route;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.flightStatus = FlightStatus.SCHEDULED;
    }

    public void updateAircraft(String newAircraftRegistration) {
        if (newAircraftRegistration == null || newAircraftRegistration.isBlank()) {
            throw new IllegalArgumentException("Aircraft registration cannot be empty");
        }
        if (this.flightStatus == FlightStatus.ARRIVED) {
            throw new IllegalStateException("Cannot change aircraft of completed flight");
        }
        this.aircraftRegistration = newAircraftRegistration;
    }

    public void changeRoute(Route newRoute) {
        if (newRoute == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        if (this.flightStatus == FlightStatus.ARRIVED) {
            throw new IllegalStateException("Cannot change route of completed flight");
        }
        this.route = newRoute;
    }

    public void startFlight() {
        if (flightStatus != FlightStatus.SCHEDULED) {
            throw new IllegalStateException("Flight cannot be started");
        }
        this.flightStatus = FlightStatus.IN_PROGRESS;
        this.timeOfDeparture = LocalDateTime.now();
    }

    public void completeFlight() {
        if (this.flightStatus != FlightStatus.IN_PROGRESS) {
            throw new IllegalStateException("Flight is not in progress");
        }
        this.flightStatus = FlightStatus.ARRIVED;
        this.timeOfArrival = LocalDateTime.now();
    }

    public void cancelFlight() {
        if (flightStatus == FlightStatus.ARRIVED) {
            throw new IllegalStateException("Completed flight cannot be cancelled");
        }
        this.flightStatus = FlightStatus.CANCELED;
    }

    public boolean isActive() {
        return flightStatus == FlightStatus.IN_PROGRESS;
    }

    public Long getFlightId() {
        return flightId;
    }

    public Route getRoute() {
        return route;
    }

    public String getAircraftRegistration() {
        return aircraftRegistration;
    }

    public FlightStatus getFlightStatus() {
        return flightStatus;
