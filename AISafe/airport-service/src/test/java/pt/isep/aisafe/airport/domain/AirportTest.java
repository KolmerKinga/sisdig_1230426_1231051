package pt.isep.aisafe.airport.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AirportTest {

    private static AirportLocation porto() {
        return new AirportLocation("Porto", "Portugal", "Porto District", "Europe/Lisbon", 41.24, -8.68);
    }

    private static Airport opo() {
        return new Airport("OPO", "Francisco Sá Carneiro Airport", "International", AirportStatus.OPERATIONAL,
                "00:00-24:00", porto(), new Facilities(1, 35, List.of("WiFi")),
                List.of(new Runway("17/35", 3480.0, "Asphalt", RunwayStatus.IN_USE)),
                List.of(), List.of(new Contact(ContactType.EMAIL, "info@ana.pt")), List.of());
    }

    @Test
    void createsValidAirport() {
        Airport airport = opo();

        assertThat(airport.getIataCode()).isEqualTo("OPO");
        assertThat(airport.isOperational()).isTrue();
        assertThat(airport.getRunways()).hasSize(1);
    }

    @Test
    void rejectsInvalidIataCode() {
        assertThatThrownBy(() -> new Airport("op", "x", null, AirportStatus.OPERATIONAL, null, porto(),
                new Facilities(1, 1, List.of()), List.of(), List.of(), List.of(), List.of()))
                .isInstanceOf(InvalidAirportException.class);
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        assertThatThrownBy(() -> new AirportLocation("X", "Y", "Z", "UTC", 95.0, 0.0))
                .isInstanceOf(InvalidAirportException.class);
    }

    @Test
    void changesStatus() {
        Airport airport = opo();

        airport.changeStatus(AirportStatus.CLOSED);

        assertThat(airport.getStatus()).isEqualTo(AirportStatus.CLOSED);
        assertThat(airport.isOperational()).isFalse();
    }

    @Test
    void partialDetailsUpdateKeepsNullFields() {
        Airport airport = opo();

        airport.updateDetails(null, "06:00-23:00", null);

        assertThat(airport.getName()).isEqualTo("Francisco Sá Carneiro Airport");
        assertThat(airport.getOperationalHours()).isEqualTo("06:00-23:00");
        assertThat(airport.getContacts()).hasSize(1);
    }

    @Test
    void certificationCannotExpireBeforeStart() {
        assertThatThrownBy(() -> new Certification("ICAO", "SAFETY",
                LocalDate.of(2026, 1, 1), LocalDate.of(2025, 1, 1)))
                .isInstanceOf(InvalidAirportException.class);
    }

    @Test
    void certificationValidityWindow() {
        Certification certification = new Certification("ICAO", "SAFETY",
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(certification.isValidOn(LocalDate.of(2025, 6, 1))).isTrue();
        assertThat(certification.isValidOn(LocalDate.of(2027, 1, 1))).isFalse();
    }
}
