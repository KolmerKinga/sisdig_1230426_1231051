package pt.isep.aisafe.aircraft.api.dto;

public record UpdateAircraftSpecsRequest(
        Double fuelCapacityLiters,
        Double maximumRangeKm,
        Double cruisingSpeedKph,
        Integer standardSeatingCapacity
) {}
