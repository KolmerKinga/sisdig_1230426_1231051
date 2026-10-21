package pt.isep.aisafe.airport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Runway of an airport. Only reachable through its Airport aggregate.
 * The id is a UUID so it never collides between replicas.
 */
@Entity
@Table(name = "airport_runways")
public class Runway {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String runwayName;

    @Column(nullable = false)
    private Double length;

    private String orientation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunwayStatus status;

    protected Runway() {
        // for JPA
    }

    public Runway(String runwayName, Double length, String orientation, RunwayStatus status) {
        if (runwayName == null || runwayName.isBlank()) {
            throw new InvalidAirportException("Runway name is required");
        }
        if (length == null || length <= 0) {
            throw new InvalidAirportException("Runway length must be positive");
        }
        if (status == null) {
            throw new InvalidAirportException("Runway status is required");
        }
        this.runwayName = runwayName;
        this.length = length;
        this.orientation = orientation;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getRunwayName() {
        return runwayName;
    }

    public Double getLength() {
        return length;
    }

    public String getOrientation() {
        return orientation;
    }

    public RunwayStatus getStatus() {
        return status;
    }
}
