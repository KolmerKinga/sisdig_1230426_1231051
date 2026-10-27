package pt.isep.aisafe.aircraft.api.dto;

import pt.isep.aisafe.aircraft.domain.AircraftAvailability;
import java.time.LocalDate;
import java.util.List;

public record AircraftResponse(
        String registrationNumber,
        String modelName,
        String manufacturer,
        AircraftAvailability status,
        LocalDate manufacturingDate,
        Double totalOperationalHours,
        Double totalFlightHours,
        List<String> features
) {}
