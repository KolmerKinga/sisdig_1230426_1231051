package com.example.AISafePSOFT_26.WP3A.US112;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Route.RouteController;
import com.example.AISafePSOFT_26.Route.application.RouteExportService;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteHistory;
import com.example.AISafePSOFT_26.Route.domain.RouteRequirements;
import com.example.AISafePSOFT_26.Route.domain.RouteStatus;
import com.example.AISafePSOFT_26.Route.domain.RouteType;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class UpdateRouteHttpTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteService routeService;

    @MockBean
    private RouteExportService routeExportService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldUpdateRouteStatusAndFlightTime() throws Exception {
        Route route = route(RouteStatus.INACTIVE, 1.5);
        when(routeService.updateRoute(1L, "LIS", 1.5, RouteStatus.INACTIVE))
                .thenReturn(route);

        String json = """
        {
          "destinationIataCode": "LIS",
          "estimatedFlightTimeHours": 1.5,
          "status": "INACTIVE"
        }
        """;

        mockMvc.perform(patch("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.estimatedFlightTimeHours").value(1.5));

        verify(routeService, times(1))
                .updateRoute(1L, "LIS", 1.5, RouteStatus.INACTIVE);
    }

    private Route route(RouteStatus status, Double flightTime) {
        Airport origin = mock(Airport.class);
        Airport destination = mock(Airport.class);
        when(origin.getIataCode()).thenReturn("OPO");
        when(destination.getIataCode()).thenReturn("LIS");

        return new Route(
                new RouteRequirements(300.0, 120),
                status,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2026, 5, 21), 0),
                flightTime,
                origin,
                destination,
                "OPO-LIS"
        );
    }
}
