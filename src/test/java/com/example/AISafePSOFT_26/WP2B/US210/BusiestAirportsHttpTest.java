package com.example.AISafePSOFT_26.WP2B.US210;

import com.example.AISafePSOFT_26.Airport.AirportController;
import com.example.AISafePSOFT_26.Airport.application.AirportCsvService;
import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.*;
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

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusiestAirportsHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AirportService airportService;

    @MockBean
    private AirportCsvService airportCsvService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnBusiestAirportsSuccessfully() throws Exception {

        Airport lisAirport = new Airport(
                "LIS",
                "International",
                "Humberto Delgado Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Lisbon", -9.1359, 38.7742,
                        "Lisbon", "Europe/Lisbon", "Portugal"),
                new Facilities(2, List.of("fuel"), 48),
                0.0,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        Airport opoAirport = new Airport(
                "OPO",
                "International",
                "Francisco Sá Carneiro Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Porto", -8.6814, 41.2481,
                        "Norte", "Europe/Lisbon", "Portugal"),
                new Facilities(1, List.of("fuel"), 36),
                0.0,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        AirportService.AirportRouteStatistic stat1 = new AirportService.AirportRouteStatistic(lisAirport, 5L);
        AirportService.AirportRouteStatistic stat2 = new AirportService.AirportRouteStatistic(opoAirport, 3L);

        when(airportService.busiestAirports())
                .thenReturn(List.of(stat1, stat2));

        mockMvc.perform(get("/api/airports/busiest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].iataCode").value("LIS"))
                .andExpect(jsonPath("$[0].numberOfRoutes").value(5))
                .andExpect(jsonPath("$[1].iataCode").value("OPO"))
                .andExpect(jsonPath("$[1].numberOfRoutes").value(3));

        verify(airportService, times(1)).busiestAirports();
    }

    @Test
    void shouldReturnEmptyListWhenNoBusiestAirports() throws Exception {

        when(airportService.busiestAirports())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/airports/busiest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(airportService, times(1)).busiestAirports();
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        when(airportService.busiestAirports())
                .thenThrow(new RuntimeException("Database failure"));

        mockMvc.perform(get("/api/airports/busiest"))
                .andExpect(status().isInternalServerError());
    }
}

