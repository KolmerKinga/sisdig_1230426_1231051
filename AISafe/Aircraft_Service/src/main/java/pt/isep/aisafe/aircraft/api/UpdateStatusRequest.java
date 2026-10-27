package pt.isep.aisafe.aircraft.api.dto;

import pt.isep.aisafe.aircraft.domain.AircraftAvailability;

public record UpdateStatusRequest(
        AircraftAvailability status
) {}
