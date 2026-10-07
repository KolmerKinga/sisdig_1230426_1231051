package com.example.AISafePSOFT_26.WP4B.US221;

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
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class TurnaroundAverageHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MaintenanceReportService maintenanceReportService;
    @MockBean private MaintenanceAlertService maintenanceAlertService;
    @MockBean private MaintenanceRecordProgressService maintenanceRecordProgressService;
    @MockBean private MaintenanceTemplateSearchService maintenanceTemplateSearchService;
    @MockBean private AddMaintenanceTemplateUseCase addMaintenanceTemplateUseCase;
    @MockBean private AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    @MockBean private AircraftSearchService aircraftSearchService;
    @MockBean private MaintenanceRecordSearchService maintenanceRecordSearchService;
    @MockBean private PartsInventoryService partsInventoryService;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnTurnaroundAverages() throws Exception {
        when(maintenanceReportService.averageTurnaroundTimePerAircraftType()).thenReturn(List.of(
                new MaintenanceReportService.TurnaroundAverage("Airbus A320", 3.0),
                new MaintenanceReportService.TurnaroundAverage("Boeing 737", 7.5)
        ));

        mockMvc.perform(get("/api/maintenance/turnaround-average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aircraftType").value("Airbus A320"))
                .andExpect(jsonPath("$[0].averageDays").value(3.0))
                .andExpect(jsonPath("$[1].aircraftType").value("Boeing 737"))
                .andExpect(jsonPath("$[1].averageDays").value(7.5));

        verify(maintenanceReportService, times(1)).averageTurnaroundTimePerAircraftType();
    }

    @Test
    void shouldReturnEmptyListWhenNoData() throws Exception {
        when(maintenanceReportService.averageTurnaroundTimePerAircraftType()).thenReturn(List.of());

        mockMvc.perform(get("/api/maintenance/turnaround-average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}