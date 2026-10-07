package com.example.AISafePSOFT_26.WP3B.US213;

import com.example.AISafePSOFT_26.Flight.domain.Flight;
import com.example.AISafePSOFT_26.Flight.infrastructure.FlightRepository;
import com.example.AISafePSOFT_26.Route.application.RouteService;
import com.example.AISafePSOFT_26.Route.infrastructure.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetScheduledFlightsForAircraftUseCaseFromRepoTest {
    private FlightRepository flightRepository;
    private RouteService useCase;

    @BeforeEach
    void setUp() {
        flightRepository = mock(FlightRepository.class);
        useCase = new RouteService(mock(RouteRepository.class), flightRepository);
    }

    @Test
    void shouldGetScheduledFlightsForAircraft() {
        Flight flight = mock(Flight.class);
        when(flightRepository.findByAircraft_RegistrationNumber("CS-TST"))
                .thenReturn(List.of(flight));

        List<Flight> result = useCase.getScheduledFlightsForAircraft("CS-TST");

        assertEquals(1, result.size());
        assertEquals(flight, result.get(0));
        verify(flightRepository, times(1)).findByAircraft_RegistrationNumber("CS-TST");
    }
}
