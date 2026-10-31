package pt.isep.aisafe.routeflight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RouteRequirements {

    @Column(name = "required_range")
    private Double requiredRange;

    @Column(name = "required_capacity")
    private Integer requiredCapacity;

    protected RouteRequirements() {}

    public RouteRequirements(Double requiredRange, Integer requiredCapacity) {
        if (requiredRange != null && requiredRange <= 0) {
            throw new IllegalArgumentException("Required range must be positive");
        }
        if (requiredCapacity != null && requiredCapacity <= 0) {
            throw new IllegalArgumentException("Required capacity must be positive");
        }

        this.requiredRange = requiredRange;
        this.requiredCapacity = requiredCapacity;
    }

    public Double getRequiredRange() {
        return requiredRange;
    }

    public Integer getRequiredCapacity() {
        return requiredCapacity;
    }
}
