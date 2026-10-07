package com.example.AISafePSOFT_26.WP2B.US208;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class UpdateAirportDetailsHttpTest {

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
    void shouldUpdateAirportDetailsSuccessfully() throws Exception {

        Airport airport = new Airport(
                "LIS",
                "International",
                "Lisbon Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Lisbon", -9.1359, 38.7742,
                        "Lisbon", "Europe/Lisbon", "Portugal"),
                new Facilities(2, List.of("fuel"), 48),
                0.0,
                List.of(),
                List.of(),
                List.of(new Contact(ContactType.PHONE, "+351210000000")),
                List.of()
        );

        when(airportService.updateDetails(
                eq("LIS"),
                eq("Lisbon Airport"),
                eq("05:00-00:00"),
                any()
        )).thenReturn(airport);

        String json = """
        {
            "name": "Lisbon Airport",
            "operationalHours": "05:00-00:00",
            "contacts": [
                {
                    "type": "PHONE",
                    "value": "+351210000000"
                }
            ]
        }
        """;

        mockMvc.perform(patch("/api/airports/LIS/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lisbon Airport"))
                .andExpect(jsonPath("$.iataCode").value("LIS"));

        verify(airportService, times(1))
                .updateDetails(eq("LIS"), eq("Lisbon Airport"), eq("05:00-00:00"), any());
    }

    @Test
    void shouldReturn200WithPartialUpdate() throws Exception {

        Airport airport = new Airport(
                "LIS",
                "International",
                "Updated Name",
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

        when(airportService.updateDetails(
                eq("LIS"),
                eq("Updated Name"),
                eq(null),
                any()
        )).thenReturn(airport);

        String json = """
        {
            "name": "Updated Name"
        }
        """;

        mockMvc.perform(patch("/api/airports/LIS/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(airportService, times(1))
                .updateDetails(eq("LIS"), eq("Updated Name"), any(), any());
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        when(airportService.updateDetails(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database failure"));

        String json = """
        {
            "name": "Lisbon Airport",
            "operationalHours": "05:00-00:00"
        }
        """;

        mockMvc.perform(patch("/api/airports/LIS/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isInternalServerError());
    }
}

