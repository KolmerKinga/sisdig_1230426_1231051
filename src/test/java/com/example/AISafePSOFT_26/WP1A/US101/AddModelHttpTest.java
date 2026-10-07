package com.example.AISafePSOFT_26.WP1A.US101;

import com.example.AISafePSOFT_26.Aircraft.application.AircraftSearchService;
import com.example.AISafePSOFT_26.AircraftCatalog.ModelCatalogController;
import com.example.AISafePSOFT_26.AircraftCatalog.application.AddModelUseCase;
import com.example.AISafePSOFT_26.AircraftCatalog.application.AircraftModelSearchService;
import com.example.AISafePSOFT_26.AircraftCatalog.application.AircraftModelUpdater;
import com.example.AISafePSOFT_26.Flight.application.FlightSearchService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModelCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddModelHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddModelUseCase addModelUseCase;

    @MockBean
    private AircraftModelSearchService aircraftModelSearchService;

    @MockBean
    private AircraftModelUpdater aircraftModelUpdater;

    @MockBean
    private FlightSearchService flightSearchService;

    @MockBean
    private AircraftSearchService aircraftSearchService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturn201WhenModelIsCreated() throws Exception {

        String json = """
        {
            "modelName": "Airbus A320",
            "modelImage": [
                "https://example.com/a320.jpg"
            ],
            "manufacturer": "Airbus",
            "specs": {
                "fuelCapacityLiters": 24210.0,
                "maximumRangeKm": 6150.0,
                "cruisingSpeedKph": 828.0,
                "standardSeatingCapacity": 180
            }
        }
        """;

        mockMvc.perform(post("/api/catalog/model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(addModelUseCase, times(1))
                .execute(any());
    }

    @Test
    void shouldReturn400WhenModelNameIsBlank() throws Exception {

        String json = """
        {
            "modelName": "",
            "modelImage": [
                "https://example.com/a320.jpg"
            ],
            "manufacturer": "Airbus",
            "specs": {
                "fuelCapacityLiters": 24210.0,
                "maximumRangeKm": 6150.0,
                "cruisingSpeedKph": 828.0,
                "standardSeatingCapacity": 180
            }
        }
        """;

        mockMvc.perform(post("/api/catalog/model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        doThrow(new RuntimeException("Database failure"))
                .when(addModelUseCase)
                .execute(any());

        String json = """
        {
            "modelName": "Airbus A320",
            "modelImage": [
                "https://example.com/a320.jpg"
            ],
            "manufacturer": "Airbus",
            "specs": {
                "fuelCapacityLiters": 24210.0,
                "maximumRangeKm": 6150.0,
                "cruisingSpeedKph": 828.0,
                "standardSeatingCapacity": 180
            }
        }
        """;

        mockMvc.perform(post("/api/catalog/model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }
}