package com.example.AISafePSOFT_26.WP1A.US105;

import com.example.AISafePSOFT_26.Aircraft.HangarController;
import com.example.AISafePSOFT_26.Aircraft.application.AddAircraftUseCase;
import com.example.AISafePSOFT_26.Aircraft.application.AircraftLifeCycleUpdaterService;
import com.example.AISafePSOFT_26.Aircraft.application.AircraftSearchService;
import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.domain.AircraftAvailability;
import com.example.AISafePSOFT_26.Aircraft.infrastructure.CalculationsService;
import com.example.AISafePSOFT_26.AircraftCatalog.application.AircraftModelSearchService;
import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftModel;
import com.example.AISafePSOFT_26.Aircraft.application.FuelEfficiencyService;
import com.example.AISafePSOFT_26.Flight.application.FlightSearchService;
import com.example.AISafePSOFT_26.Route.application.RouteSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatchAircraftFromRepoTest {

    private AircraftSearchService aircraftSearchService;
    private AircraftLifeCycleUpdaterService aircraftLifeCycleUpdaterService;
    private HangarController hangarController;

    @BeforeEach
    void setUp() {

        aircraftSearchService = mock(AircraftSearchService.class);

        aircraftLifeCycleUpdaterService =
                new AircraftLifeCycleUpdaterService();

        hangarController = new HangarController(
                mock(AddAircraftUseCase.class),
                mock(AircraftModelSearchService.class),
                aircraftLifeCycleUpdaterService,
                aircraftSearchService,
                mock(RouteSearchService.class),
                mock(FlightSearchService.class),
                mock(FuelEfficiencyService.class)
        );
    }

    @Test
    void shouldChangeAircraftAvailability() {

        AircraftModel model = mock(AircraftModel.class);
        when(model.getModelName()).thenReturn("Airbus A320");

        Aircraft aircraft = new Aircraft(
                "CS-TST",
                model,
                LocalDate.of(2020, 1, 1),
                1000.0,
                800.0
        );

        when(aircraftSearchService.spotAircraftInHangar("CS-TST"))
                .thenReturn(Optional.of(aircraft));

        hangarController.changeAircraft(
                "CS-TST",
                new HangarController.PatchAircraftAvailabilityRequest(
                        "CS-TST",
                        "AVAILABLE"
                )
        );

        assertEquals(AircraftAvailability.AVAILABLE, aircraft.getStatus());

        verify(aircraftSearchService, times(1))
                .spotAircraftInHangar("CS-TST");
    }
}