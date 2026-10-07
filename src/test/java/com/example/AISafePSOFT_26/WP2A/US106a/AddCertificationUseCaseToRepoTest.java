package com.example.AISafePSOFT_26.WP2A.US106a;

import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.*;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddCertificationUseCaseToRepoTest {
    private AirportRepository airportRepository;
    private AirportService useCase;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        useCase = new AirportService(airportRepository, mock(RouteRepository.class));
    }

    @Test
    void shouldAddCertificationToAirportAndSaveIt() {
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

        Certification certification = useCase.addCertification(
                "LIS",
                "EASA Aerodrome Certificate",
                "SAFETY",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 1, 1)
        );

        assertEquals("EASA Aerodrome Certificate", certification.getName());
        assertEquals("SAFETY", certification.getCategory());
        assertEquals(1, airport.getCertifications().size());
        assertEquals(airport, certification.getAirport());
        verify(airportRepository, times(1)).save(airport);
    }
}
