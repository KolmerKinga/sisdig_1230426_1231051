package com.example.AISafePSOFT_26.WP3A.US110;

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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreateRouteHttpTest {
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
    void shouldReturn201WhenRouteIsCreated() throws Exception {
        Route route = route("OPO-LIS");
        when(routeService.createRoute(
                eq("OPO-LIS"),
                eq("OPO"),
                eq("LIS"),
                eq(1.0),
                eq(RouteType.DIRECT),
                any(RouteRequirements.class)
        )).thenReturn(route);

        String json = """
        {
          "routeName": "OPO-LIS",
          "originIataCode": "OPO",
          "destinationIataCode": "LIS",
          "estimatedFlightTimeHours": 1.0,
          "type": "DIRECT",
          "requirements": {
            "requiredRange": 300.0,
            "requiredCapacity": 120
          }
        }
        """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeName").value("OPO-LIS"))
                .andExpect(jsonPath("$.originIataCode").value("OPO"))
                .andExpect(jsonPath("$.destinationIataCode").value("LIS"));

        verify(routeService, times(1)).createRoute(
                eq("OPO-LIS"),
                eq("OPO"),
                eq("LIS"),
                eq(1.0),
                eq(RouteType.DIRECT),
                any(RouteRequirements.class)
        );
    }

    @Test
    void shouldReturn409WhenRequirementsAreMissing() throws Exception {
        String json = """
        {
          "routeName": "OPO-LIS",
          "originIataCode": "OPO",
          "destinationIataCode": "LIS",
          "estimatedFlightTimeHours": 1.0,
          "type": "DIRECT"
        }
        """;

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        verifyNoInteractions(routeService);
    }

    private Route route(String routeName) {
        Airport origin = mock(Airport.class);
        Airport destination = mock(Airport.class);
        when(origin.getIataCode()).thenReturn("OPO");
        when(destination.getIataCode()).thenReturn("LIS");

        return new Route(
                new RouteRequirements(300.0, 120),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2026, 5, 21), 0),
                1.0,
                origin,
                destination,
                routeName
        );
    }
}
