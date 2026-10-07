package com.example.AISafePSOFT_26.BONUS.US228;

import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.domain.AirportLocation;
import com.example.AISafePSOFT_26.Airport.domain.AirportStatus;
import com.example.AISafePSOFT_26.Route.application.RouteExportService;
import com.example.AISafePSOFT_26.Route.domain.Route;
import com.example.AISafePSOFT_26.Route.domain.RouteHistory;
import com.example.AISafePSOFT_26.Route.domain.RouteRequirements;
import com.example.AISafePSOFT_26.Route.domain.RouteStatus;
import com.example.AISafePSOFT_26.Route.domain.RouteType;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteExportUseCaseTest {

    private RouteRepository routeRepository;
    private RouteExportService service;

    private Airport lisbon;
    private Airport porto;
    private Route route;

    @BeforeEach
    void setUp() {
        routeRepository = mock(RouteRepository.class);
        service = new RouteExportService(routeRepository);

        lisbon = new Airport("LIS", "large_airport", "Humberto Delgado Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Lisbon", -9.1393, 38.7223, "PT-11", "Europe/Lisbon", "Portugal"),
                null, null, null, null, null, null);

        porto = new Airport("OPO", "large_airport", "Francisco Sá Carneiro Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Porto", -8.6739, 41.2358, "PT-13", "Europe/Lisbon", "Portugal"),
                null, null, null, null, null, null);

        route = new Route(
                new RouteRequirements(400.0, 150),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2024, 1, 1), 0),
                1.5,
                312.0,
                lisbon,
                porto,
                "LIS-OPO"
        );
    }

    @Test
    void shouldExportEmptyGeoJsonWhenNoRoutes() {
        when(routeRepository.findAll()).thenReturn(List.of());

        String result = service.exportAsGeoJson();

        assertTrue(result.contains("\"type\": \"FeatureCollection\""));
        assertTrue(result.contains("\"features\": ["));
        assertFalse(result.contains("LIS-OPO"));
    }

    @Test
    void shouldExportGeoJsonWithRouteFeature() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        String result = service.exportAsGeoJson();

        assertTrue(result.contains("\"type\": \"FeatureCollection\""));
        assertTrue(result.contains("\"type\": \"Feature\""));
        assertTrue(result.contains("\"type\": \"LineString\""));
        assertTrue(result.contains("\"LIS-OPO\""));
        assertTrue(result.contains("\"origin\": \"LIS\""));
        assertTrue(result.contains("\"destination\": \"OPO\""));
        assertTrue(result.contains("\"status\": \"ACTIVE\""));
        assertTrue(result.contains("-9.1393"));
        assertTrue(result.contains("38.7223"));
    }

    @Test
    void shouldExportEmptyKmlDocumentWhenNoRoutes() {
        when(routeRepository.findAll()).thenReturn(List.of());

        String result = service.exportAsKml();

        assertTrue(result.contains("<?xml version=\"1.0\""));
        assertTrue(result.contains("<kml"));
        assertTrue(result.contains("<Document>"));
        assertTrue(result.contains("<name>Route Network</name>"));
        assertFalse(result.contains("<Placemark>"));
    }

    @Test
    void shouldExportKmlWithRoutePlacemark() {
        when(routeRepository.findAll()).thenReturn(List.of(route));

        String result = service.exportAsKml();

        assertTrue(result.contains("<kml"));
        assertTrue(result.contains("<Placemark>"));
        assertTrue(result.contains("<name>LIS-OPO</name>"));
        assertTrue(result.contains("ACTIVE | 312.0 km"));
        assertTrue(result.contains("<LineString>"));
        assertTrue(result.contains("<coordinates>"));
        assertTrue(result.contains("-9.1393,38.7223"));
        assertTrue(result.contains("-8.6739,41.2358"));
    }

    @Test
    void shouldExportMultipleRoutesInGeoJson() {
        Airport faro = new Airport("FAO", "medium_airport", "Faro Airport",
                AirportStatus.OPERATIONAL,
                new AirportLocation("Faro", -7.9659, 37.0144, "PT-08", "Europe/Lisbon", "Portugal"),
                null, null, null, null, null, null);

        Route route2 = new Route(
                new RouteRequirements(300.0, 100),
                RouteStatus.ACTIVE,
                RouteType.DIRECT,
                new RouteHistory(LocalDate.of(2024, 1, 1), 0),
                1.2,
                280.0,
                lisbon,
                faro,
                "LIS-FAO"
        );

        when(routeRepository.findAll()).thenReturn(List.of(route, route2));

        String result = service.exportAsGeoJson();

        assertTrue(result.contains("LIS-OPO"));
        assertTrue(result.contains("LIS-FAO"));
    }
}
