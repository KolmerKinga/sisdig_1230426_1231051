package com.example.AISafePSOFT_26.Aircraft.application;

import com.example.AISafePSOFT_26.Aircraft.domain.Aircraft;
import com.example.AISafePSOFT_26.Aircraft.domain.AircraftAvailability;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AircraftLifeCycleUpdaterService {
    public void changeAvailability(Aircraft aircraft, AircraftAvailability newStatus) {
        switch (newStatus) {
            case AVAILABLE -> aircraft.activateAircraft();
            case MAINTENANCE -> aircraft.sendToMaintenance();
            case INACTIVE -> aircraft.retireAircraft();
            default -> throw new IllegalArgumentException(
                    "Unsupported status transition"
            );
        }
    }
}