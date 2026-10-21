package pt.isep.aisafe.airport.domain;

/**
 * Raised when an airport invariant is violated (mapped to HTTP 400).
 */
public class InvalidAirportException extends RuntimeException {
    public InvalidAirportException(String message) {
        super(message);
    }
}
