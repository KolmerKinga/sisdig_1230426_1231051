package pt.isep.aisafe.aircraft.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository modelRepository;

    @InjectMocks
    private AircraftService aircraftService;

    private AircraftModel sampleModel;

    @BeforeEach
    void setUp() {
        AircraftSpecs specs = new AircraftSpecs(24000.0, 6100.0, 840.0, 180);
        sampleModel = new AircraftModel("A320-200", "Airbus", specs, List.of());
    }

    @Test
    void registersAircraftSuccessfully() {
        when(modelRepository.findById("A320-200")).thenReturn(Optional.of(sampleModel));
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aircraft result = aircraftService.registerAircraft(
                "CS-TMA", "A320-200", LocalDate.of(2022, 1, 1), 500.0, 400.0
        );

        assertThat(result).isNotNull();
        assertThat(result.getRegistrationNumber()).isEqualTo("CS-TMA");
        assertThat(result.getStatus()).isEqualTo(AircraftAvailability.AVAILABLE);
        verify(aircraftRepository, times(1)).save(any(Aircraft.class));
    }

    @Test
    void throwsExceptionWhenRegistrationFailsIfModelNotFound() {
        when(modelRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aircraftService.registerAircraft(
                "CS-TMA", "UNKNOWN", LocalDate.now(), 100.0, 50.0
        )).isInstanceOf(AircraftModelNotFoundException.class);
    }

    @Test
    void updatesAircraftStatusSuccessfully() {
        Aircraft aircraft = new Aircraft("CS-TMA", sampleModel, LocalDate.now(), 500.0, 400.0);

        lenient().when(aircraftRepository.findById("CS-TMA")).thenReturn(Optional.of(aircraft));
        lenient().when(aircraftRepository.findByRegistrationNumber("CS-TMA")).thenReturn(Optional.of(aircraft));
        when(aircraftRepository.save(any(Aircraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aircraft updated = aircraftService.updateStatus("CS-TMA", AircraftAvailability.MAINTENANCE);

        assertThat(updated.getStatus()).isEqualTo(AircraftAvailability.MAINTENANCE);
        verify(aircraftRepository).save(aircraft);
    }

    @Test
    void searchesAircraftByStatus() {
        Aircraft aircraft = new Aircraft("CS-TMA", sampleModel, LocalDate.now(), 500.0, 400.0);
        when(aircraftRepository.findByStatus(AircraftAvailability.AVAILABLE)).thenReturn(List.of(aircraft));

        List<Aircraft> result = aircraftService.searchAircraft(AircraftAvailability.AVAILABLE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRegistrationNumber()).isEqualTo("CS-TMA");
    }
}
