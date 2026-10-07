package com.example.AISafePSOFT_26.BONUS.US229;

import com.example.AISafePSOFT_26.Aircraft.application.AircraftSearchService;
import com.example.AISafePSOFT_26.Flight.FlightController;
import com.example.AISafePSOFT_26.Flight.application.FlightUtilizationReportService;
import com.example.AISafePSOFT_26.Route.application.RouteService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteUtilizationReportHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @MockBean
    private AircraftSearchService aircraftSearchService;

    @MockBean
    private FlightUtilizationReportService flightUtilizationReportService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnRouteUtilizationReport() throws Exception {
        FlightUtilizationReportService.RouteUtilizationReport entry =
                new FlightUtilizationReportService.RouteUtilizationReport(
                        1L, "LIS-OPO", "LIS", "OPO", 3L);

        when(flightUtilizationReportService.getRouteUtilizationReport())
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/flights/reports/route-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1))
                .andExpect(jsonPath("$[0].routeName").value("LIS-OPO"))
                .andExpect(jsonPath("$[0].originIata").value("LIS"))
                .andExpect(jsonPath("$[0].destinationIata").value("OPO"))
                .andExpect(jsonPath("$[0].flightCount").value(3));

        verify(flightUtilizationReportService, times(1)).getRouteUtilizationReport();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoFlights() throws Exception {
        when(flightUtilizationReportService.getRouteUtilizationReport())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/flights/reports/route-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
