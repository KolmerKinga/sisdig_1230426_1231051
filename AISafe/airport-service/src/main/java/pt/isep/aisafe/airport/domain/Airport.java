package pt.isep.aisafe.airport.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root of the Airport Management bounded context.
 *
 * <p>The IATA code is the natural identifier and is the only value other services keep
 * (Route/Flight Operations references airports by IATA code, never through JPA).</p>
 */
@Entity
@Table(name = "airports")
public class Airport {

    @Id
    @Column(length = 3, nullable = false, updatable = false)
    private String iataCode;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    private String airportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportStatus status;

    private String operationalHours;

    @Embedded
    private AirportLocation location;

    @Embedded
    private Facilities facilities;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "airport_iata_code")
    private List<Runway> runways = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "airport_iata_code")
    private List<Certification> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_contacts", joinColumns = @JoinColumn(name = "airport_iata_code"))
    private List<Contact> contacts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_photos", joinColumns = @JoinColumn(name = "airport_iata_code"))
    @Column(name = "photo_url")
    private List<String> airportPhotos = new ArrayList<>();

    protected Airport() {
        // for JPA
    }

    public Airport(String iataCode, String name, String airportType, AirportStatus status,
                   String operationalHours, AirportLocation location, Facilities facilities,
                   List<Runway> runways, List<Certification> certifications,
                   List<Contact> contacts, List<String> airportPhotos) {
        if (iataCode == null || !iataCode.matches("[A-Z]{3}")) {
            throw new InvalidAirportException("IATA code must have exactly 3 uppercase letters");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidAirportException("Airport name is required");
        }
        if (status == null) {
            throw new InvalidAirportException("Airport status is required");
        }
        if (location == null) {
            throw new InvalidAirportException("Airport location is required");
        }
        if (facilities == null) {
            throw new InvalidAirportException("Airport facilities are required");
        }
        this.iataCode = iataCode;
        this.name = name;
        this.airportType = airportType;
        this.status = status;
        this.operationalHours = operationalHours;
        this.location = location;
        this.facilities = facilities;
        this.runways = copyOf(runways);
        this.certifications = copyOf(certifications);
        this.contacts = copyOf(contacts);
        this.airportPhotos = copyOf(airportPhotos);
    }

    public void changeStatus(AirportStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidAirportException("Airport status is required");
        }
        this.status = newStatus;
    }

    /**
     * Partial update (PATCH semantics): a null argument keeps the current value.
     */
    public void updateDetails(String newName, String newOperationalHours, List<Contact> newContacts) {
        if (newName != null) {
            if (newName.isBlank()) {
                throw new InvalidAirportException("Airport name cannot be blank");
            }
            this.name = newName;
        }
        if (newOperationalHours != null) {
            this.operationalHours = newOperationalHours;
        }
        if (newContacts != null) {
            this.contacts.clear();
            this.contacts.addAll(newContacts);
        }
    }

    public void addCertification(Certification certification) {
        if (certification == null) {
            throw new InvalidAirportException("Certification is required");
        }
        this.certifications.add(certification);
    }

    public boolean isOperational() {
        return status == AirportStatus.OPERATIONAL;
    }

    public String getIataCode() {
        return iataCode;
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getAirportType() {
        return airportType;
    }

    public AirportStatus getStatus() {
        return status;
    }

    public String getOperationalHours() {
        return operationalHours;
    }

    public AirportLocation getLocation() {
        return location;
    }

    public Facilities getFacilities() {
        return facilities;
    }

    public List<Runway> getRunways() {
        return Collections.unmodifiableList(runways);
    }

    public List<Certification> getCertifications() {
        return Collections.unmodifiableList(certifications);
    }

    public List<Contact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    public List<String> getAirportPhotos() {
        return Collections.unmodifiableList(airportPhotos);
    }

    private static <T> List<T> copyOf(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
