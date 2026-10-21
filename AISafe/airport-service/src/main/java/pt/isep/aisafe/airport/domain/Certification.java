package pt.isep.aisafe.airport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Certification held by an airport (e.g. ICAO Annex 14 compliance).
 * Distinct from AircraftCertification, which belongs to Aircraft Management.
 */
@Entity
@Table(name = "airport_certifications")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate startsAt;

    @Column(nullable = false)
    private LocalDate expiresAt;

    protected Certification() {
        // for JPA
    }

    public Certification(String name, String category, LocalDate startsAt, LocalDate expiresAt) {
        if (name == null || name.isBlank()) {
            throw new InvalidAirportException("Certification name is required");
        }
        if (category == null || category.isBlank()) {
            throw new InvalidAirportException("Certification category is required");
        }
        if (startsAt == null || expiresAt == null) {
            throw new InvalidAirportException("Certification start and expiry dates are required");
        }
        if (expiresAt.isBefore(startsAt)) {
            throw new InvalidAirportException("Certification cannot expire before it starts");
        }
        this.name = name;
        this.category = category;
        this.startsAt = startsAt;
        this.expiresAt = expiresAt;
    }

    public boolean isValidOn(LocalDate date) {
        return !date.isBefore(startsAt) && !date.isAfter(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getStartsAt() {
        return startsAt;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }
}
