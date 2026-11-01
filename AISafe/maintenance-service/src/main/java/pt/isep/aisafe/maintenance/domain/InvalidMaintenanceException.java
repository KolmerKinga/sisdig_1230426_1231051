package pt.isep.aisafe.maintenance.domain;

/**
 * Invalid data for a maintenance template or record (HTTP 400).
 */
public class InvalidMaintenanceException extends RuntimeException {
    public InvalidMaintenanceException(String message) {
        super(message);
    }
}
