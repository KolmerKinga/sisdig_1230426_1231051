package pt.isep.aisafe.route_flight.domain;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @Column(name = "route_id", nullable = false, updatable = false)
    private String routeId;

    @Column(name = "route_name", nullable = false, unique = true)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RouteType type;

    @Column(name = "origin_iata", nullable = false, length = 3)
    private String originIata;

    @Column(name = "destination_iata", nullable = false, length = 3)
    private String destinationIata;

    @Column(name = "estimated_time_hours", nullable = false)
    private Double estimatedTimeHours;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Embedded
    private RouteRequirements requirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RouteStatus status;

    protected Route() {}

    public Route(String routeName, RouteType type, String originIata, String destinationIata,
                 Double estimatedTimeHours, Double distanceKm, RouteRequirements requirements) {
        this.routeId = UUID.randomUUID().toString();
        this.routeName = routeName;
        this.type = type;
        this.originIata = originIata;
        this.destinationIata = destinationIata;
        this.estimatedTimeHours = estimatedTimeHours;
        this.distanceKm = distanceKm;
        this.requirements = requirements;
        this.status = RouteStatus.ACTIVE;
    }

    public Route(RouteRequirements requirements, RouteStatus status, RouteType type,
                 Object routeHistory, Double estimatedTimeHours, Double distanceKm,
                 String originIata, String destinationIata, String routeName) {
        this.routeId = UUID.randomUUID().toString();
        this.routeName = routeName;
        this.type = type;
        this.originIata = originIata;
        this.destinationIata = destinationIata;
        this.estimatedTimeHours = estimatedTimeHours;
        this.distanceKm = distanceKm;
        this.requirements = requirements;
        this.status = status != null ? status : RouteStatus.ACTIVE;
    }

    public String getRouteId() { return routeId; }
    public String getRouteName() { return routeName; }
    public RouteType getType() { return type; }
    public String getOriginIata() { return originIata; }
    public String getOriginAirportIata() { return originIata; }
    public String getDestinationIata() { return destinationIata; }
    public String getDestinationAirportIata() { return destinationIata; }
    public Double getEstimatedTimeHours() { return estimatedTimeHours; }
    public Double getEstimatedFlightTimeHours() { return estimatedTimeHours; }
    public Double getDistanceKm() { return distanceKm; }
    public RouteRequirements getRequirements() { return requirements; }
    public RouteRequirements getRouteRequirements() { return requirements; }
    public Object getRouteHistory() { return null; }
    public RouteStatus getStatus() { return status; }

    public boolean isOperational() { return status == RouteStatus.ACTIVE; }
    public void activate() { this.status = RouteStatus.ACTIVE; }
    public void activateRoute() { this.status = RouteStatus.ACTIVE; }
    public void deactivate() { this.status = RouteStatus.INACTIVE; }
    public void suspendRoute() { this.status = RouteStatus.INACTIVE; }
    public void retireRoute() { this.status = RouteStatus.INACTIVE; }

    public void setRouteName(String routeName) { this.routeName = routeName; }
    public void setType(RouteType type) { this.type = type; }
    public void setOriginIata(String originIata) { this.originIata = originIata; }
    public void setDestinationIata(String destinationIata) { this.destinationIata = destinationIata; }
    public void setEstimatedTimeHours(Double estimatedTimeHours) { this.estimatedTimeHours = estimatedTimeHours; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public void setRequirements(RouteRequirements requirements) { this.requirements = requirements; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(routeId, route.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }
}
