package pt.isep.aisafe.route_flight.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @Column(name = "flight_id", nullable = false, updatable = false)
    private String flightId;

    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Column(name = "aircraft_registration", nullable = false)
    private String aircraftRegistration;

    @Column(name = "scheduled_departure", nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival", nullable = false)
    private LocalDateTime scheduledArrival;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlightStatus status;

    protected Flight() {}

    public Flight(String routeId, String aircraftRegistration, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this.flightId = UUID.randomUUID().toString();
        this.routeId = routeId;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.status = FlightStatus.SCHEDULED;
    }

    public Flight(Route route, String aircraftRegistration, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this.flightId = UUID.randomUUID().toString();
        this.routeId = route != null ? route.getRouteId() : null;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.status = FlightStatus.SCHEDULED;
    }

    public String getFlightId() { return flightId; }
    public String getRouteId() { return routeId; }
    public String getRoute() { return routeId; }
    public void setRoute(String routeId) { this.routeId = routeId; }
    public void setRoute(Route route) { this.routeId = route != null ? route.getRouteId() : null; }

    public String getAircraftRegistration() { return aircraftRegistration; }
    public LocalDateTime getScheduledDeparture() { return scheduledDeparture; }
    public LocalDateTime getTimeOfDeparture() { return scheduledDeparture; }
    public LocalDateTime getScheduledArrival() { return scheduledArrival; }
    public LocalDateTime getTimeOfArrival() { return scheduledArrival; }
    public FlightStatus getStatus() { return status; }
    public FlightStatus getFlightStatus() { return status; }

    public Double getFlightHours() {
        if (scheduledDeparture != null && scheduledArrival != null) {
            return Duration.between(scheduledDeparture, scheduledArrival).toMinutes() / 60.0;
        }
        return 0.0;
    }

    public void startFlight() { this.status = FlightStatus.IN_AIR; }
    public void completeFlight() { this.status = FlightStatus.COMPLETED; }
    public void cancelFlight() { this.status = FlightStatus.CANCELLED; }
    public void cancel() { this.status = FlightStatus.CANCELLED; }
    public void complete() { this.status = FlightStatus.COMPLETED; }
    public void markInAir() { this.status = FlightStatus.IN_AIR; }
    public void markDelayed() { this.status = FlightStatus.DELAYED; }

    public void updateSchedule(LocalDateTime departure, LocalDateTime arrival) {
        this.scheduledDeparture = departure;
        this.scheduledArrival = arrival;
    }

    public void assignAircraft(String aircraftRegistration) {
        this.aircraftRegistration = aircraftRegistration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightId, flight.flightId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId);
    }
}
