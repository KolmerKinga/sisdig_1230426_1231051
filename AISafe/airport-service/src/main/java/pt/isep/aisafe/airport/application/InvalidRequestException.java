package pt.isep.aisafe.airport.application;

/**
 * Invalid query parameters or input that is not a domain rule (HTTP 400).
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
