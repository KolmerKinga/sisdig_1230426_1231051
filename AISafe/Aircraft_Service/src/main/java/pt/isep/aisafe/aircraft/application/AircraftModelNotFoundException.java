package pt.isep.aisafe.aircraft.application;

public class AircraftModelNotFoundException extends RuntimeException {
    public AircraftModelNotFoundException(String modelName) {
        super("Aircraft model '" + modelName + "' was not found.");
    }
}
