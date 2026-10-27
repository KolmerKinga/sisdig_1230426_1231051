package pt.isep.aisafe.aircraft.api.dto;

import java.util.List;

public record AircraftModelResponse(
        String modelName,
        String manufacturer,
        Double fuelCapacityLiters,
        Double maximumRangeKm,
        Double cruisingSpeedKph,
        Integer standardSeatingCapacity,
        List<String> modelImages
) {}
