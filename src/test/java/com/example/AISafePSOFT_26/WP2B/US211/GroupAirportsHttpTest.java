package com.example.AISafePSOFT_26.WP2B.US211;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupAirportsHttpTest {

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
    void shouldGroupAirportsByCountrySuccessfully() throws Exception {

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

        Map<String, List<Airport>> groupedByCountry = new HashMap<>();
        groupedByCountry.put("Portugal", List.of(lisAirport, opoAirport));

        when(airportService.groupAirportsBy(eq("country")))
                .thenReturn(groupedByCountry);

        mockMvc.perform(get("/api/airports/grouped")
                .param("by", "country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Portugal.length()").value(2))
                .andExpect(jsonPath("$.Portugal[0].iataCode").value("LIS"))
                .andExpect(jsonPath("$.Portugal[1].iataCode").value("OPO"));

        verify(airportService, times(1)).groupAirportsBy("country");
    }

    @Test
    void shouldReturnEmptyGroupWhenNoAirports() throws Exception {

        Map<String, List<Airport>> emptyGroups = new HashMap<>();

        when(airportService.groupAirportsBy(eq("country")))
                .thenReturn(emptyGroups);

        mockMvc.perform(get("/api/airports/grouped")
                .param("by", "country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());

        verify(airportService, times(1)).groupAirportsBy("country");
    }

    @Test
    void shouldUseDefaultGroupingWhenParameterNotProvided() throws Exception {

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

        Map<String, List<Airport>> groupedByCountry = new HashMap<>();
        groupedByCountry.put("Portugal", List.of(lisAirport));

        when(airportService.groupAirportsBy(eq("country")))
                .thenReturn(groupedByCountry);

        mockMvc.perform(get("/api/airports/grouped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Portugal.length()").value(1));

        verify(airportService, times(1)).groupAirportsBy("country");
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        when(airportService.groupAirportsBy(eq("country")))
                .thenThrow(new RuntimeException("Database failure"));

        mockMvc.perform(get("/api/airports/grouped")
                .param("by", "country"))
                .andExpect(status().isInternalServerError());
    }
}

