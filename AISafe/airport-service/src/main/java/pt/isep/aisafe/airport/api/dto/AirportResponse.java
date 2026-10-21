package pt.isep.aisafe.airport.api.dto;

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
import java.util.UUID;

/**
 * Airport representation returned by the API and exchanged between replicas
 * (it is also deserialized when aggregating peer answers).
 * Other services (route-flight-service) read only the fields they need: iataCode, status,
 * location and certifications.
 */
public record AirportResponse(
        String iataCode,
        String name,
        String airportType,
        AirportStatus status,
        boolean operational,
        String operationalHours,
        LocationResponse location,
        FacilitiesResponse facilities,
        List<RunwayResponse> runways,
        List<CertificationResponse> certifications,
        List<ContactResponse> contacts,
        List<String> airportPhotos) {

    public record LocationResponse(String city, String country, String region, String timezone,
                                   Double latitude, Double longitude) {
        static LocationResponse from(AirportLocation location) {
            return new LocationResponse(location.getCity(), location.getCountry(), location.getRegion(),
                    location.getTimezone(), location.getLatitude(), location.getLongitude());
        }
    }

    public record FacilitiesResponse(Integer terminalCount, Integer gateCount, List<String> services) {
        static FacilitiesResponse from(Facilities facilities) {
            return new FacilitiesResponse(facilities.getTerminalCount(), facilities.getGateCount(),
                    List.copyOf(facilities.getServices()));
        }
    }

    public record RunwayResponse(UUID id, String runwayName, Double length, String orientation,
                                 RunwayStatus status) {
        static RunwayResponse from(Runway runway) {
            return new RunwayResponse(runway.getId(), runway.getRunwayName(), runway.getLength(),
                    runway.getOrientation(), runway.getStatus());
        }
    }

    public record CertificationResponse(UUID id, String name, String category, LocalDate startsAt,
                                        LocalDate expiresAt, boolean valid) {
        public static CertificationResponse from(Certification certification) {
            return new CertificationResponse(certification.getId(), certification.getName(),
                    certification.getCategory(), certification.getStartsAt(), certification.getExpiresAt(),
                    certification.isValidOn(LocalDate.now()));
        }
    }

    public record ContactResponse(ContactType type, String value) {
        static ContactResponse from(Contact contact) {
            return new ContactResponse(contact.getType(), contact.getValue());
        }
    }

    public static AirportResponse from(Airport airport) {
        return new AirportResponse(
                airport.getIataCode(),
                airport.getName(),
                airport.getAirportType(),
                airport.getStatus(),
                airport.isOperational(),
                airport.getOperationalHours(),
                LocationResponse.from(airport.getLocation()),
                FacilitiesResponse.from(airport.getFacilities()),
                airport.getRunways().stream().map(RunwayResponse::from).toList(),
                airport.getCertifications().stream().map(CertificationResponse::from).toList(),
                airport.getContacts().stream().map(ContactResponse::from).toList(),
                List.copyOf(airport.getAirportPhotos()));
    }
}
