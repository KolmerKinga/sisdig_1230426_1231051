package pt.isep.aisafe.maintenance.infrastructure.client;

/** No replica of a service this service depends on could be reached (HTTP 503). */
public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) {
        super(message);
    }
}
