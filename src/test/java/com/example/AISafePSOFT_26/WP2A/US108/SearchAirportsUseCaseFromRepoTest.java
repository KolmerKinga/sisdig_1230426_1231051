package com.example.AISafePSOFT_26.WP2A.US108;

import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchAirportsUseCaseFromRepoTest {
    private AirportRepository airportRepository;
    private AirportService useCase;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        useCase = new AirportService(airportRepository, mock(RouteRepository.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSearchAirportsMatchingFilters() {
        Airport airport = mock(Airport.class);
        when(airportRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(airport));

        List<Airport> result =
                useCase.searchAirports("Lisbon", "Portugal", "Humberto Delgado Airport");

        assertEquals(1, result.size());
        assertEquals(airport, result.get(0));
        verify(airportRepository, times(1)).findAll(any(Specification.class));
    }
}