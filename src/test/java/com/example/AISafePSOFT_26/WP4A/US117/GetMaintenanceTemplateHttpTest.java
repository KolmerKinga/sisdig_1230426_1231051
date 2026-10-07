package com.example.AISafePSOFT_26.WP4A.US117;

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
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceAttribute;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceTemplate;
import com.example.AISafePSOFT_26.Maintenance.domain.MaintenanceType;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class GetMaintenanceTemplateHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MaintenanceTemplateSearchService maintenanceTemplateSearchService;
    @MockBean private MaintenanceReportService maintenanceReportService;
    @MockBean private MaintenanceAlertService maintenanceAlertService;
    @MockBean private MaintenanceRecordProgressService maintenanceRecordProgressService;
    @MockBean private AddMaintenanceTemplateUseCase addMaintenanceTemplateUseCase;
    @MockBean private AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    @MockBean private AircraftSearchService aircraftSearchService;
    @MockBean private MaintenanceRecordSearchService maintenanceRecordSearchService;
    @MockBean private PartsInventoryService partsInventoryService;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetTemplateByIdSuccessfully() throws Exception {
        MaintenanceTemplate template = new MaintenanceTemplate(
                "Engine Check",
                2.0,
                Map.of("oil_change", true),
                MaintenanceType.INSPECTION,
                MaintenanceAttribute.ENGINE
        );

        when(maintenanceTemplateSearchService.findByTemplateId(1L)).thenReturn(Optional.of(template));

        mockMvc.perform(get("/api/maintenance/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engine Check"))
                .andExpect(jsonPath("$.expectedDuration").value(2.0));

        verify(maintenanceTemplateSearchService, times(1)).findByTemplateId(1L);
    }

    @Test
    void shouldListTemplates() throws Exception {
        MaintenanceTemplate t1 = new MaintenanceTemplate(
                "Engine Check",
                2.0,
                Map.of("oil_change", true),
                MaintenanceType.INSPECTION,       // era NORMAL  → não existe
                MaintenanceAttribute.ENGINE        // era SAFETY  → não existe
        );

        MaintenanceTemplate t2 = new MaintenanceTemplate(
                "Landing Gear",
                3.5,
                Map.of("brakes", true),
                MaintenanceType.SCHEDULED,
                MaintenanceAttribute.AIRFRAME
        );

        when(maintenanceTemplateSearchService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/maintenance/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Engine Check"))
                .andExpect(jsonPath("$[1].name").value("Landing Gear"));

        verify(maintenanceTemplateSearchService, times(1)).findAll();
    }

    @Test
    void shouldReturn409WhenTemplateNotFound() throws Exception {
        when(maintenanceTemplateSearchService.findByTemplateId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/maintenance/templates/99"))
                .andExpect(status().isConflict());

        verify(maintenanceTemplateSearchService, times(1)).findByTemplateId(99L);
    }
}