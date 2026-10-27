package pt.isep.aisafe.aircraft.api.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateAircraftRequest(
        String registrationNumber,
        String modelName,
        LocalDate manufacturingDate,
        Double totalOperationalHours,
        Double totalFlightHours,
        List<String> features
) {}
