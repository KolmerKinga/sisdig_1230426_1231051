package pt.isep.aisafe.aircraft.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AircraftSpecs {

    @Column(name = "fuel_capacity_liters", nullable = false)
    private Double fuelCapacityLiters;

    @Column(name = "maximum_range_km", nullable = false)
    private Double maximumRangeKm;

    @Column(name = "cruising_speed_kph", nullable = false)
    private Double cruisingSpeedKph;

    @Column(name = "standard_seating_capacity", nullable = false)
    private Integer standardSeatingCapacity;

    protected AircraftSpecs() {}

    public AircraftSpecs(Double fuelCapacityLiters, Double maximumRangeKm,
                         Double cruisingSpeedKph, Integer standardSeatingCapacity) {
        if (fuelCapacityLiters == null || fuelCapacityLiters <= 0) {
            throw new IllegalArgumentException("Fuel capacity must be positive");
        }
        if (maximumRangeKm == null || maximumRangeKm <= 0) {
            throw new IllegalArgumentException("Maximum range must be positive");
        }
        if (cruisingSpeedKph == null || cruisingSpeedKph <= 0) {
            throw new IllegalArgumentException("Cruising speed must be positive");
        }
        if (standardSeatingCapacity == null || standardSeatingCapacity <= 0) {
            throw new IllegalArgumentException("Standard seating capacity must be positive");
        }

        this.fuelCapacityLiters = fuelCapacityLiters;
        this.maximumRangeKm = maximumRangeKm;
        this.cruisingSpeedKph = cruisingSpeedKph;
        this.standardSeatingCapacity = standardSeatingCapacity;
    }

    public Double getFuelCapacity() {
        return fuelCapacityLiters;
    }

    public Double getMaximumRange() {
        return maximumRangeKm;
    }

    public Double getCruisingSpeed() {
        return cruisingSpeedKph;
    }

    public Integer getStandardSeatingCapacity() {
        return standardSeatingCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftSpecs that = (AircraftSpecs) o;
        return Objects.equals(fuelCapacityLiters, that.fuelCapacityLiters) &&
                Objects.equals(maximumRangeKm, that.maximumRangeKm) &&
                Objects.equals(cruisingSpeedKph, that.cruisingSpeedKph) &&
                Objects.equals(standardSeatingCapacity, that.standardSeatingCapacity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fuelCapacityLiters, maximumRangeKm, cruisingSpeedKph, standardSeatingCapacity);
    }
}
