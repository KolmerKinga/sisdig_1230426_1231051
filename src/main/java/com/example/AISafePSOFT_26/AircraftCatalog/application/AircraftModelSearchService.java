package com.example.AISafePSOFT_26.AircraftCatalog.application;

import com.example.AISafePSOFT_26.AircraftCatalog.domain.AircraftModel;
import com.example.AISafePSOFT_26.AircraftCatalog.infrastructure.AircraftModelRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AircraftModelSearchService {
    private final AircraftModelRepository aircraftModelRepository;

    public AircraftModelSearchService(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public Optional<AircraftModel> spotAircraftInCatalog(String modelName) {
        return aircraftModelRepository.findByModelName(modelName);
    }
}