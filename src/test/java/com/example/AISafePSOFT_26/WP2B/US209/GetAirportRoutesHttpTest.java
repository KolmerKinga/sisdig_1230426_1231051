package com.example.AISafePSOFT_26.WP2B.US209;

import com.example.AISafePSOFT_26.Airport.AirportController;
import com.example.AISafePSOFT_26.Airport.application.AirportCsvService;
import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.*;
import com.example.AISafePSOFT_26.Route.domain.*;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
class GetAirportRoutesHttpTest {

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
    void shouldReturnAirportRoutesSuccessfully() throws Exception {

        Airport originAirport = new Airport(
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

        Airport destAirport = new Airport(
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

        Route route = new Route(
                new RouteRequirements(3200.0, 160),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2024, 1, 1), 0),
                0.9,
                originAirport,
                destAirport,
                "LIS-OPO"
        );

        when(airportService.findRoutesByAirport(eq("LIS")))
                .thenReturn(List.of(route));

        mockMvc.perform(get("/api/airports/LIS/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].routeName").value("LIS-OPO"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].type").value("DIRECT"))
                .andExpect(jsonPath("$[0].originIataCode").value("LIS"))
                .andExpect(jsonPath("$[0].destinationIataCode").value("OPO"));

        verify(airportService, times(1))
                .findRoutesByAirport("LIS");
    }

    @Test
    void shouldReturnEmptyListWhenNoRoutesFound() throws Exception {

        when(airportService.findRoutesByAirport(eq("LIS")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/airports/LIS/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(airportService, times(1))
                .findRoutesByAirport("LIS");
    }

    @Test
    void shouldReturnMultipleRoutes() throws Exception {

        Airport airport = new Airport(
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

        Airport destAirport1 = new Airport(
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

        Airport destAirport2 = new Airport(
                "MAD",
                "International",
                "Madrid Barajas Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Madrid", -3.5625, 40.4719,
                        "Madrid", "Europe/Madrid", "Spain"),
                new Facilities(4, List.of("fuel", "maintenance"), 100),
                0.0,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        Route route1 = new Route(
                new RouteRequirements(3200.0, 160),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2024, 1, 1), 0),
                0.9,
                airport,
                destAirport1,
                "LIS-OPO"
        );

        Route route2 = new Route(
                new RouteRequirements(4000.0, 180),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2024, 2, 1), 0),
                1.5,
                airport,
                destAirport2,
                "LIS-MAD"
        );

        when(airportService.findRoutesByAirport(eq("LIS")))
                .thenReturn(List.of(route1, route2));

        mockMvc.perform(get("/api/airports/LIS/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].routeName").value("LIS-OPO"))
                .andExpect(jsonPath("$[1].routeName").value("LIS-MAD"))
                .andExpect(jsonPath("$[0].estimatedFlightTimeHours").value(0.9))
                .andExpect(jsonPath("$[1].estimatedFlightTimeHours").value(1.5));

        verify(airportService, times(1))
                .findRoutesByAirport("LIS");
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {

        when(airportService.findRoutesByAirport(eq("LIS")))
                .thenThrow(new RuntimeException("Database failure"));

        mockMvc.perform(get("/api/airports/LIS/routes"))
                .andExpect(status().isInternalServerError());
    }
}

