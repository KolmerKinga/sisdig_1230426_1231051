package pt.isep.aisafe.airport.application;

/**
 * An airport with the same IATA code already exists on this replica or on a peer (HTTP 409).
 */
public class AirportAlreadyExistsException extends RuntimeException {
    public AirportAlreadyExistsException(String iataCode, String where) {
        super("Airport " + iataCode + " already exists (" + where + ")");
    }
}
