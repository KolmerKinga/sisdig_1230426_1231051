package com.example.AISafePSOFT_26.WP3A.US111;

import com.example.AISafePSOFT_26.Route.RouteController;
import com.example.AISafePSOFT_26.Route.application.RouteExportService;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.domain.RouteHistory;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class GetRouteHistoryHttpTest {
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
    void shouldReturnRouteHistory() throws Exception {
        when(routeService.getRouteHistory(1L))
                .thenReturn(new RouteHistory(LocalDate.of(2026, 5, 21), 3));

        mockMvc.perform(get("/api/routes/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeBegin").value("2026-05-21"))
                .andExpect(jsonPath("$.routeUsage").value(3));

        verify(routeService, times(1)).getRouteHistory(1L);
    }
}
