package com.example.AISafePSOFT_26.BONUS.US228;

import com.example.AISafePSOFT_26.Route.RouteController;
import com.example.AISafePSOFT_26.Route.application.RouteExportService;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.authUsers.application.JwtService;
import com.example.AISafePSOFT_26.authUsers.infrastructure.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteExportHttpTest {

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
    void shouldReturnGeoJsonWithCorrectContentType() throws Exception {
        String geoJson = "{\"type\": \"FeatureCollection\", \"features\": []}";
        when(routeExportService.exportAsGeoJson()).thenReturn(geoJson);

        mockMvc.perform(get("/api/routes/export/geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/geo+json"))
                .andExpect(content().string(geoJson));

        verify(routeExportService, times(1)).exportAsGeoJson();
    }

    @Test
    void shouldReturnKmlWithCorrectContentType() throws Exception {
        String kml = "<?xml version=\"1.0\"?><kml><Document><name>Route Network</name></Document></kml>";
        when(routeExportService.exportAsKml()).thenReturn(kml);

        mockMvc.perform(get("/api/routes/export/kml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.google-earth.kml+xml"))
                .andExpect(content().string(kml));

        verify(routeExportService, times(1)).exportAsKml();
    }

    @Test
    void shouldReturnEmptyGeoJsonFeatureCollection() throws Exception {
        String emptyGeoJson = "{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n    \n  ]\n}";
        when(routeExportService.exportAsGeoJson()).thenReturn(emptyGeoJson);

        mockMvc.perform(get("/api/routes/export/geojson"))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyGeoJson));
    }

    @Test
    void shouldReturnEmptyKmlDocument() throws Exception {
        String emptyKml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n  <Document>\n    <name>Route Network</name>\n    \n  </Document>\n</kml>";
        when(routeExportService.exportAsKml()).thenReturn(emptyKml);

        mockMvc.perform(get("/api/routes/export/kml"))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyKml));
    }
}
