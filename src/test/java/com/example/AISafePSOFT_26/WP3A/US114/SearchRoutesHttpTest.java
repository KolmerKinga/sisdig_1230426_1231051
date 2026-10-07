package com.example.AISafePSOFT_26.WP3A.US114;

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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchRoutesHttpTest {
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
    void shouldSearchRoutesByOriginAndDestination() throws Exception {
        Route r = route("OPO-LIS");
        when(routeService.searchRoutes("OPO", "LIS")).thenReturn(List.of(r));

        mockMvc.perform(get("/api/routes")
                        .param("origin", "OPO")
                        .param("destination", "LIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeName").value("OPO-LIS"))
                .andExpect(jsonPath("$[0].originIataCode").value("OPO"))
                .andExpect(jsonPath("$[0].destinationIataCode").value("LIS"));

        verify(routeService, times(1)).searchRoutes("OPO", "LIS");
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
