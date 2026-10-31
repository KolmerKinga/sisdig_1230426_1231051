package pt.isep.aisafe.routeflight.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "company_routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @Version
    private Long version;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RouteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RouteType type;

    @Column(name = "origin_airport_iata", nullable = false)
    private String originAirportIata;

    @Column(name = "destination_airport_iata", nullable = false)
    private String destinationAirportIata;

    @Embedded
    private RouteHistory routeHistory;

    @Column(name = "estimated_flight_time_hours", nullable = false)
    private Double estimatedFlightTimeHours;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Embedded
    private RouteRequirements routeRequirements;

    protected Route() {}

    public Route(RouteRequirements routeRequirements, RouteStatus status, RouteType type,
                 RouteHistory routeHistory, Double estimatedFlightTimeHours,
                 String originAirportIata, String destinationAirportIata, String routeName) {
        this(routeRequirements, status, type, routeHistory, estimatedFlightTimeHours,
                1.0, originAirportIata, destinationAirportIata, routeName);
    }

    public Route(RouteRequirements routeRequirements, RouteStatus status, RouteType type,
                 RouteHistory routeHistory, Double estimatedFlightTimeHours, Double distanceKm,
                 String originAirportIata, String destinationAirportIata, String routeName) {
        if (originAirportIata == null || originAirportIata.isBlank()) {
            throw new IllegalArgumentException("Origin airport IATA code cannot be empty");
        }
        if (destinationAirportIata == null || destinationAirportIata.isBlank()) {
            throw new IllegalArgumentException("Destination airport IATA code cannot be empty");
        }
        if (originAirportIata.trim().equalsIgnoreCase(destinationAirportIata.trim())) {
            throw new IllegalArgumentException("Origin and destination airports cannot be equal");
        }
        if (estimatedFlightTimeHours == null || estimatedFlightTimeHours <= 0) {
            throw new IllegalArgumentException("Estimated flight time must be positive");
        }
        if (distanceKm == null || distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }

        this.routeRequirements = routeRequirements;
        this.status = status != null ? status : RouteStatus.ACTIVE;
        this.type = type;
        this.routeHistory = routeHistory;
        this.estimatedFlightTimeHours = estimatedFlightTimeHours;
        this.distanceKm = distanceKm;
        this.originAirportIata = originAirportIata.trim().toUpperCase();
        this.destinationAirportIata = destinationAirportIata.trim().toUpperCase();
        this.routeName = routeName;
    }

    public void activateRoute() {
        if (status == RouteStatus.ARCHIVED) {
            throw new IllegalStateException("Archived routes cannot be activated");
        }
        this.status = RouteStatus.ACTIVE;
    }

    public void suspendRoute() {
        if (status == RouteStatus.ARCHIVED) {
            throw new IllegalStateException("Archived routes cannot be suspended");
        }
        this.status = RouteStatus.INACTIVE;
    }

    public void retireRoute() {
        this.status = RouteStatus.ARCHIVED;
    }

    public void updateEstimatedFlightTime(Double estimatedFlightTimeHours) {
        if (estimatedFlightTimeHours == null || estimatedFlightTimeHours <= 0) {
            throw new IllegalArgumentException("Estimated flight time must be positive");
        }
        this.estimatedFlightTimeHours = estimatedFlightTimeHours;
    }

    public void changeDestination(String newDestinationIata) {
        if (newDestinationIata == null || newDestinationIata.isBlank()) {
            throw new IllegalArgumentException("Destination airport IATA code cannot be empty");
        }
        if (newDestinationIata.trim().equalsIgnoreCase(originAirportIata)) {
            throw new IllegalArgumentException("Destination airport cannot equal origin airport");
        }
        this.destinationAirportIata = newDestinationIata.trim().toUpperCase();
    }

    public void updateRoute(String routeName, RouteStatus status, RouteType type,
                            RouteHistory routeHistory, Double estimatedFlightTimeHours, Double distanceKm,
                            String originAirportIata, String destinationAirportIata,
                            RouteRequirements routeRequirements) {
        if (originAirportIata == null || originAirportIata.isBlank()) {
            throw new IllegalArgumentException("Origin airport IATA code cannot be empty");
        }
        if (destinationAirportIata == null || destinationAirportIata.isBlank()) {
            throw new IllegalArgumentException("Destination airport IATA code cannot be empty");
        }
        if (originAirportIata.trim().equalsIgnoreCase(destinationAirportIata.trim())) {
            throw new IllegalArgumentException("Origin and destination airports cannot be equal");
        }
        if (estimatedFlightTimeHours == null || estimatedFlightTimeHours <= 0) {
            throw new IllegalArgumentException("Estimated flight time must be positive");
        }
        if (distanceKm == null || distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }

        this.routeName = routeName;
        this.status = status;
        this.type = type;
        this.
