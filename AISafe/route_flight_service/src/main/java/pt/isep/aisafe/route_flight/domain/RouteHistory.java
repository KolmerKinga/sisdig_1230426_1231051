package pt.isep.aisafe.routeflight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class RouteHistory {

    @Column(name = "route_begin", nullable = false)
    private LocalDate routeBegin;

    @Column(name = "route_finish")
    private LocalDate routeFinish;

    @Column(name = "route_usage")
    private Integer routeUsage;

    protected RouteHistory() {}

    public RouteHistory(LocalDate routeBegin, Integer routeUsage) {
        if (routeBegin == null) {
            throw new IllegalArgumentException("Route begin date cannot be null");
        }
        this.routeBegin = routeBegin;
        this.routeUsage = routeUsage != null ? routeUsage : 0;
    }

    public void endRoute(LocalDate routeFinish) {
        if (routeFinish != null && routeFinish.isBefore(routeBegin)) {
            throw new IllegalArgumentException("Route finish date cannot be before route begin date");
        }
        this.routeFinish = routeFinish;
    }

    public void incrementUsage() {
        if (this.routeUsage == null) {
            this.routeUsage = 0;
        }
        this.routeUsage++;
    }

    public void updateRouteUsage(Integer usageIncrement) {
        if (usageIncrement == null || usageIncrement < 0) {
            throw new IllegalArgumentException("Usage increment must be non-negative");
        }
        if (this.routeUsage == null) {
            this.routeUsage = 0;
        }
        this.routeUsage += usageIncrement;
    }

    public LocalDate getRouteBegin() {
        return routeBegin;
    }

    public LocalDate getRouteFinish() {
        return routeFinish;
    }

    public Integer getRouteUsage() {
        return routeUsage;
    }
}
