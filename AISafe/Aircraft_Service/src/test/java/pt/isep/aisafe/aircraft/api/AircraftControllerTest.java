package pt.isep.aisafe.aircraft.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.aisafe.aircraft.application.AircraftService;
import pt.isep.aisafe.aircraft.domain.Aircraft;
import pt.isep.aisafe.aircraft.domain.AircraftAvailability;
import pt.isep.aisafe.aircraft.domain.AircraftModel;
import pt.isep.aisafe.aircraft.domain.AircraftSpecs;
import pt.isep.aisafe.aircraft.shared.forwarding.PeerClient;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftService aircraftService;

    @MockBean
    private PeerClient peerClient;

    private AircraftModel createSampleModel(String name, String manufacturer) {
        AircraftSpecs specs = new AircraftSpecs(24000.0, 6100.0, 840.0, 180);
        return new AircraftModel(name, manufacturer, specs, List.of());
    }

    @Test
    void getsAircraftByRegistrationNumber() throws Exception {
        AircraftModel model = createSampleModel("A320-200", "Airbus");
        Aircraft aircraft = new Aircraft("CS-TMA", model, LocalDate.of(2020, 5, 10), 1200.0, 950.0);

        when(aircraftService.getAircraftByRegistration("CS-TMA")).thenReturn(aircraft);

        mockMvc.perform(get("/api/aircraft/CS-TMA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TMA"))
                .andExpect(jsonPath("$.modelName").value("A320-200"))
                .andExpect(jsonPath("$.manufacturer").value("Airbus"));
    }

    @Test
    void createsAircraftViaPost() throws Exception {
        AircraftModel model = createSampleModel("A320-200", "Airbus");
        Aircraft aircraft = new Aircraft("CS-TMC", model, LocalDate.of(2023, 1, 15), 100.0, 80.0);

        when(aircraftService.registerAircraft(any(), any(), any(), any(), any())).thenReturn(aircraft);

        String jsonPayload = """
                {
                    "registrationNumber": "CS-TMC",
                    "modelName": "A320-200",
                    "manufacturingDate": "2023-01-15",
                    "totalOperationalHours": 100.0,
                    "totalFlightHours": 80.0
                }
                """;

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TMC"));
    }

    @Test
    void updatesAircraftStatusViaPatch() throws Exception {
        AircraftModel model = createSampleModel("A320-200", "Airbus");
        Aircraft updated = new Aircraft("CS-TMA", model, LocalDate.of(2020, 5, 10), 1200.0, 950.0);
        updated.sendToMaintenance();

        when(aircraftService.updateStatus("CS-TMA", AircraftAvailability.MAINTENANCE)).thenReturn(updated);

        mockMvc.perform(patch("/api/aircraft/CS-TMA/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }
}
