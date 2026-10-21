package pt.isep.aisafe.airport.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import pt.isep.aisafe.airport.domain.Airport;
import pt.isep.aisafe.airport.domain.AirportLocation;
import pt.isep.aisafe.airport.domain.AirportStatus;
import pt.isep.aisafe.airport.domain.Certification;
import pt.isep.aisafe.airport.domain.Contact;
import pt.isep.aisafe.airport.domain.ContactType;
import pt.isep.aisafe.airport.domain.Facilities;
import pt.isep.aisafe.airport.domain.Runway;
import pt.isep.aisafe.airport.domain.RunwayStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Body of POST /api/airports (PSOFT US106 / US207). Field names follow the PSOFT API,
 * so existing requests keep working (routeDistance was removed: distances belong to routes).
 */
public record CreateAirportRequest(
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must have exactly 3 uppercase letters")
        String iataCode,
        String airportType,
        @NotBlank String name,
        @NotNull AirportStatus status,
        String operationalHours,
        @NotNull @Valid LocationRequest location,
        @NotNull @Valid FacilitiesRequest facilities,
        @NotEmpty List<@Valid RunwayRequest> runways,
        List<@Valid CertificationRequest> certifications,
        List<@Valid ContactRequest> contacts,
        List<String> airportPhotos) {

    public record LocationRequest(
            @NotBlank String city,
            @NotBlank String country,
            @NotBlank String region,
            @NotBlank String timezone,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
    }

    public record FacilitiesRequest(
            @NotNull @PositiveOrZero Integer terminalCount,
            @NotNull @PositiveOrZero Integer gateCount,
            List<String> services) {
    }

    public record RunwayRequest(
            @NotBlank String runwayName,
            @NotNull @Positive Double length,
            String orientation,
            @NotNull RunwayStatus status) {
    }

    public record CertificationRequest(
            @NotBlank String name,
            @NotBlank String category,
            @NotNull LocalDate startsAt,
            @NotNull LocalDate expiresAt) {

        public Certification toDomain() {
            return new Certification(name, category, startsAt, expiresAt);
        }
    }

    public record ContactRequest(
            @NotNull ContactType type,
            @NotBlank String value) {

        public Contact toDomain() {
            return new Contact(type, value);
        }
    }

    public Airport toDomain() {
        return new Airport(
                iataCode,
                name,
                airportType,
                status,
                operationalHours,
                new AirportLocation(location.city(), location.country(), location.region(),
                        location.timezone(), location.latitude(), location.longitude()),
                new Facilities(facilities.terminalCount(), facilities.gateCount(), facilities.services()),
                runways.stream()
                        .map(r -> new Runway(r.runwayName(), r.length(), r.orientation(), r.status()))
                        .toList(),
                certifications == null ? List.of()
                        : certifications.stream().map(CertificationRequest::toDomain).toList(),
                contacts == null ? List.of()
                        : contacts.stream().map(ContactRequest::toDomain).toList(),
                airportPhotos);
    }
}
