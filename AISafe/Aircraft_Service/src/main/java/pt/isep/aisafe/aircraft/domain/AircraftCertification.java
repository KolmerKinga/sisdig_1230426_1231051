package pt.isep.aisafe.aircraft.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "aircraft_certifications")
public class AircraftCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftCertificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_registration_number", nullable = false)
    private Aircraft aircraft;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate startAt;

    @Column(nullable = false)
    private LocalDate expiresAt;

    protected AircraftCertification() {}

    public AircraftCertification(
            String name,
            String category,
            LocalDate startAt,
            LocalDate expiresAt
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Certification name cannot be empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Certification category cannot be empty");
        }
        if (startAt == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        if (expiresAt.isBefore(startAt)) {
            throw new IllegalArgumentException("Expiration date cannot be before start date");
        }

        this.name = name;
        this.category = category;
        this.startAt = startAt;
        this.expiresAt = expiresAt;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public boolean isValid() {
        if (startAt == null || expiresAt == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return !now.isBefore(startAt) && !now.isAfter(expiresAt);
    }

    public Long getAircraftCertificationId() {
        return aircraftCertificationId;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getStartAt() {
        return startAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }
}
