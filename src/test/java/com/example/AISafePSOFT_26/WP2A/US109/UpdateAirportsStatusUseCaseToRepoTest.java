package com.example.AISafePSOFT_26.WP2A.US109;


import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.*;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateAirportStatusUseCaseToRepoTest {
    private AirportRepository airportRepository;
    private AirportService useCase;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        useCase = new AirportService(airportRepository, mock(RouteRepository.class));
    }

    @Test
    void shouldUpdateAirportStatusAndSaveIt() {
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
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.of(airport));
        when(airportRepository.save(airport)).thenReturn(airport);

        Airport result =
                useCase.updateStatus("LIS", AirportStatus.UNDER_MAINTENANCE);

        assertEquals(AirportStatus.UNDER_MAINTENANCE, result.getStatus());
        verify(airportRepository, times(1)).save(airport);
    }
}