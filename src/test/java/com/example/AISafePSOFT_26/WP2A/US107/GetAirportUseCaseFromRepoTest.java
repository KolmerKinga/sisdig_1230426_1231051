package com.example.AISafePSOFT_26.WP2A.US107;

import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.Airport;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAirportUseCaseFromRepoTest {
    private AirportRepository airportRepository;
    private AirportService useCase;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        useCase = new AirportService(airportRepository, mock(RouteRepository.class));
    }

    @Test
    void shouldGetAirportByIataCode() {
        Airport airport = mock(Airport.class);
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(airport));

        Optional<Airport> result = useCase.getAirportByIata("LIS");

        assertTrue(result.isPresent());
        assertEquals(airport, result.get());
        verify(airportRepository, times(1)).findByIataCode("LIS");
    }
}
