package pt.isep.aisafe.maintenance.infrastructure.client;

/**
 * The part of an aircraft that Maintenance Management needs (anticorruption layer).
 * Filled from the JSON returned by aircraft-service; other fields are ignored, so this service
 * never imports classes of aircraft-service.
 */
public record AircraftInfo(String registrationNumber, String modelName, String status, Double totalFlightHours) {

    public double flightHours() {
        return totalFlightHours == null ? 0.0 : totalFlightHours;
    }
}
