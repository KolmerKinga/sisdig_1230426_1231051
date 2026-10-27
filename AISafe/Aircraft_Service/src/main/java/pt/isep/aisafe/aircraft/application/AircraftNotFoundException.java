package pt.isep.aisafe.aircraft.application;

public class AircraftNotFoundException extends RuntimeException {
    public AircraftNotFoundException(String registrationNumber) {
        super("Aircraft with registration number '" + registrationNumber + "' was not found.");
    }
}
