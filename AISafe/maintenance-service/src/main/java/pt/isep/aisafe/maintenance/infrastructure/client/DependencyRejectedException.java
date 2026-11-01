package pt.isep.aisafe.maintenance.infrastructure.client;

/** A service this service depends on refused the request (e.g. 400/403) (HTTP 502). */
public class DependencyRejectedException extends RuntimeException {
    public DependencyRejectedException(String message) {
        super(message);
    }
}
