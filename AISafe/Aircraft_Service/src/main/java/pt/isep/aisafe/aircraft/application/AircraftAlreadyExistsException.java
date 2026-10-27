package pt.isep.aisafe.aircraft.application;

public class AircraftAlreadyExistsException extends RuntimeException {
    public AircraftAlreadyExistsException(String registrationNumber) {
        super("Aircraft with registration number '" + registrationNumber + "' already exists.");
    }
}
