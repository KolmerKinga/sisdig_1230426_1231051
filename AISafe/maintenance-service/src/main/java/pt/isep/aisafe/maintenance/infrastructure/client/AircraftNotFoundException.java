package pt.isep.aisafe.maintenance.infrastructure.client;

/** aircraft-service answered that the aircraft does not exist (HTTP 404). */
public class AircraftNotFoundException extends RuntimeException {
    public AircraftNotFoundException(String registrationNumber) {
        super("Aircraft " + registrationNumber + " not found in aircraft-service");
    }
}
