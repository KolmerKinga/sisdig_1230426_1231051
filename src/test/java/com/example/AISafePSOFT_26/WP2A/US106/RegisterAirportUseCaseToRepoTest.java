package com.example.AISafePSOFT_26.WP2A.US106;


import com.example.AISafePSOFT_26.Airport.application.AirportService;
import com.example.AISafePSOFT_26.Airport.domain.*;
import com.example.AISafePSOFT_26.Airport.infrastructure.AirportRepository;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterAirportUseCaseToRepoTest {
    private AirportRepository airportRepository;
    private AirportService useCase;

    @BeforeEach
    void setUp() {
        airportRepository = mock(AirportRepository.class);
        useCase = new AirportService(airportRepository, mock(RouteRepository.class));
    }

    @Test
    void shouldSaveAirportWhenExecuteWithFields() {
        when(airportRepository.findByIataCode("LIS"))
                .thenReturn(Optional.empty());

        AirportLocation location = new AirportLocation(
                "Lisbon", -9.1359, 38.7742, "Lisbon",
                "Europe/Lisbon", "Portugal"
        );
        Facilities facilities = new Facilities(
                2, List.of("fuel", "maintenance"), 48
        );
        List<Runway> runways = List.of(new Runway(
                null, "03/21", 3805.0, "NE/SW", RunwayStatus.OPEN
        ));

        useCase.execute(
                "LIS",
                "International",
                "Humberto Delgado Airport",
                AirportStatus.OPERATIONAL,
                location,
                facilities,
                0.0,
                runways,
                List.of(),
                List.of(new Contact(ContactType.EMAIL, "ops@lis.pt")),
                List.of("https://example.com/lis.jpg")
        );

        ArgumentCaptor<Airport> captor = ArgumentCaptor.forClass(Airport.class);
        verify(airportRepository, times(1)).save(captor.capture());

        Airport savedAirport = captor.getValue();
        assertEquals("LIS", savedAirport.getIataCode());
        assertEquals("International", savedAirport.getAirportType());
        assertEquals("Humberto Delgado Airport", savedAirport.getName());
        assertEquals(AirportStatus.OPERATIONAL, savedAirport.getStatus());
        assertEquals(location, savedAirport.getAirportLocation());
        assertEquals(facilities, savedAirport.getFacilities());
        assertEquals(1, savedAirport.getAirportRunways().size());
    }

    @Test
    void shouldSaveAirportWhenExecuteWithAirportObject() {
        Airport airport = mock(Airport.class);
        when(airport.getIataCode()).thenReturn("OPO");
        when(airportRepository.findByIataCode("OPO"))
                .thenReturn(Optional.empty());

        useCase.execute(airport);

        verify(airportRepository, times(1)).save(airport);
    }
}
