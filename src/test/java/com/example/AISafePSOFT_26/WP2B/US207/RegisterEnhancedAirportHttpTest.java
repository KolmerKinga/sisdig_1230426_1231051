package com.example.AISafePSOFT_26.WP2B.US207;

import com.example.AISafePSOFT_26.Airport.AirportController;
import com.example.AISafePSOFT_26.Airport.application.AirportCsvService;
import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterEnhancedAirportHttpTest {

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
    void shouldCreateEnhancedAirportWithOperationalHours() throws Exception {

        String json = """
        {
            "iataCode": "LIS",
            "airportType": "International",
            "name": "Humberto Delgado Airport",
            "status": "OPERATIONAL",
            "operationalHours": "06:00-23:00",
            "location": {
                "city": "Lisbon",
                "longitude": -9.1359,
                "latitude": 38.7742,
                "region": "Lisbon",
                "timezone": "Europe/Lisbon",
                "country": "Portugal"
            },
            "facilities": {
                "terminalCount": 2,
                "services": ["fuel", "maintenance"],
                "gateCount": 48
            },
            "routeDistance": 0.0,
            "runways": [
                {
                    "runwayName": "03/21",
                    "length": 3805.0,
                    "orientation": "NE/SW",
                    "status": "OPEN"
                }
            ],
            "certifications": [],
            "contacts": [
                {
                    "type": "EMAIL",
                    "value": "ops@lis.pt"
                }
            ],
            "airportPhotos": ["https://example.com/lis-front.jpg"]
        }
        """;

        mockMvc.perform(post("/api/airports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        verify(airportService, times(1))
                .execute(any());
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        doThrow(new RuntimeException("Database failure"))
                .when(airportService)
                .execute(any());

        String json = """
        {
            "iataCode": "LIS",
            "airportType": "International",
            "name": "Humberto Delgado Airport",
            "status": "OPERATIONAL",
            "operationalHours": "06:00-23:00",
            "location": {
                "city": "Lisbon",
                "longitude": -9.1359,
                "latitude": 38.7742,
                "region": "Lisbon",
                "timezone": "Europe/Lisbon",
                "country": "Portugal"
            },
            "facilities": {
                "terminalCount": 2,
                "services": ["fuel", "maintenance"],
                "gateCount": 48
            },
            "routeDistance": 0.0,
            "runways": [
                {
                    "runwayName": "03/21",
                    "length": 3805.0,
                    "orientation": "NE/SW",
                    "status": "OPEN"
                }
            ],
            "certifications": [],
            "contacts": [],
            "airportPhotos": []
        }
        """;

        mockMvc.perform(post("/api/airports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isInternalServerError());
    }
}


