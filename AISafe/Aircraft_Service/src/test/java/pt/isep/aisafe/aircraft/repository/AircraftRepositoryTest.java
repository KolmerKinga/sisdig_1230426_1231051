package pt.isep.aisafe.aircraft.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pt.isep.aisafe.aircraft.domain.Aircraft;
import pt.isep.aisafe.aircraft.domain.AircraftAvailability;
import pt.isep.aisafe.aircraft.domain.AircraftModel;
import pt.isep.aisafe.aircraft.domain.AircraftSpecs;
import pt.isep.aisafe.aircraft.infrastructure.AircraftModelRepository;
import pt.isep.aisafe.aircraft.infrastructure.AircraftRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AircraftRepositoryTest {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AircraftModelRepository modelRepository;

    private AircraftModel createSampleModel(String name, String manufacturer) {
        AircraftSpecs specs = new AircraftSpecs(24000.0, 6100.0, 840.0, 180);
        return new AircraftModel(name, manufacturer, specs, List.of());
    }

    @Test
    void savesAndFindsAircraftByRegistration() {
        AircraftModel model = modelRepository.save(createSampleModel("A320-200", "Airbus"));
        Aircraft aircraft = new Aircraft("CS-TMA", model, LocalDate.now(), 1000.0, 800.0);

        aircraftRepository.save(aircraft);

        Optional<Aircraft> found = aircraftRepository.findById("CS-TMA");

        assertThat(found).isPresent();
        assertThat(found.get().getRegistrationNumber()).isEqualTo("CS-TMA");
        assertThat(found.get().getModel().getModelName()).isEqualTo("A320-200");
    }

    @Test
    void findsAircraftsByStatus() {
        AircraftModel model = modelRepository.save(createSampleModel("B737-800", "Boeing"));
        Aircraft a1 = new Aircraft("CS-TMA", model, LocalDate.now(), 1000.0, 800.0);
        Aircraft a2 = new Aircraft("CS-TMB", model, LocalDate.now(), 500.0, 400.0);
        a2.sendToMaintenance();

        aircraftRepository.saveAll(List.of(a1, a2));

        List<Aircraft> available = aircraftRepository.findByStatus(AircraftAvailability.AVAILABLE);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getRegistrationNumber()).isEqualTo("CS-TMA");
    }
}
