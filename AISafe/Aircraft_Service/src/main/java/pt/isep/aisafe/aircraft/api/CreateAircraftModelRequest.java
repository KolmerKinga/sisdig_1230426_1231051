package pt.isep.aisafe.aircraft.api.dto;

import java.util.List;

public record CreateAircraftModelRequest(
        String modelName,
        String manufacturer,
        Double fuelCapacityLiters,
        Double maximumRangeKm,
        Double cruisingSpeedKph,
        Integer standardSeatingCapacity,
        List<String> modelImages
) {}
