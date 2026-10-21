package pt.isep.aisafe.airport.application;

/**
 * The airport is not stored on this replica nor on any reachable peer (HTTP 404).
 * {@code partial} is true when at least one peer could not be contacted, so the
 * answer is "not found on the reachable replicas".
 */
public class AirportNotFoundException extends RuntimeException {

    private final boolean partial;

    public AirportNotFoundException(String iataCode, boolean partial) {
        super("Airport " + iataCode + " not found" + (partial ? " on the reachable replicas" : ""));
        this.partial = partial;
    }

    public boolean isPartial() {
        return partial;
    }
}
