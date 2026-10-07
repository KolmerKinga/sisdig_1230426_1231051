package com.example.AISafePSOFT_26.WP4A.US119;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.application.AircraftSearchService;
import com.example.AISafePSOFT_26.Maintenance.MaintenanceRecordController;
import com.example.AISafePSOFT_26.Maintenance.aplication.AddMaintenanceRecordUseCase;
import com.example.AISafePSOFT_26.Maintenance.aplication.AddMaintenanceTemplateUseCase;
import com.example.AISafePSOFT_26.Maintenance.aplication.MaintenanceAlertService;
import com.example.AISafePSOFT_26.Maintenance.aplication.MaintenanceRecordProgressService;
import com.example.AISafePSOFT_26.Maintenance.aplication.MaintenanceReportService;
import com.example.AISafePSOFT_26.Maintenance.aplication.MaintenanceRecordSearchService;
import com.example.AISafePSOFT_26.Maintenance.aplication.MaintenanceTemplateSearchService;
import com.example.AISafePSOFT_26.Maintenance.aplication.PartsInventoryService;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceRecord;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceStatus;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaintenanceRecordHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MaintenanceRecordProgressService maintenanceRecordProgressService;
    @MockBean private MaintenanceReportService maintenanceReportService;
    @MockBean private MaintenanceAlertService maintenanceAlertService;
    @MockBean private MaintenanceTemplateSearchService maintenanceTemplateSearchService;
    @MockBean private AddMaintenanceTemplateUseCase addMaintenanceTemplateUseCase;
    @MockBean private AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    @MockBean private AircraftSearchService aircraftSearchService;
    @MockBean private MaintenanceRecordSearchService maintenanceRecordSearchService;
    @MockBean private PartsInventoryService partsInventoryService;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetRecordByIdSuccessfully() throws Exception {
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn("CS-TVA");

        MaintenanceTemplate template = new MaintenanceTemplate("Engine Check", 2.0, java.util.Map.of(), com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceType.INSPECTION, com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceAttribute.ENGINE);

        MaintenanceRecord record = new MaintenanceRecord(aircraft, template, 2.0, "desc", LocalDate.of(2026,5,1));

        when(maintenanceRecordProgressService.findById(1L)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/maintenance/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aircraftRegistration").value("CS-TVA"))
                .andExpect(jsonPath("$.description").value("desc"));

        verify(maintenanceRecordProgressService, times(1)).findById(1L);
    }

    @Test
    void shouldAdvanceStatusSuccessfully() throws Exception {
        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getRegistrationNumber()).thenReturn("CS-TVA");

        MaintenanceTemplate template = new MaintenanceTemplate("Engine Check", 2.0, java.util.Map.of(), com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceType.INSPECTION, com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceAttribute.ENGINE);

        MaintenanceRecord updated = new MaintenanceRecord(aircraft, template, 2.0, "desc", LocalDate.of(2026,5,1));
        // simulate status change
        updated.markAsOngoing();

        when(maintenanceRecordProgressService.advanceStatus(1L, MaintenanceStatus.ONGOING)).thenReturn(updated);

        mockMvc.perform(patch("/api/maintenance/records/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"ONGOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONGOING"));

        verify(maintenanceRecordProgressService, times(1)).advanceStatus(1L, MaintenanceStatus.ONGOING);
    }

    @Test
    void shouldReturn409WhenRecordNotFound() throws Exception {
        when(maintenanceRecordProgressService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/maintenance/records/99"))
                .andExpect(status().isConflict());

        verify(maintenanceRecordProgressService, times(1)).findById(99L);
    }
}


