package pt.isep.aisafe.maintenance.application;

import java.util.UUID;

/**
 * The record is not on this replica nor on any reachable peer (HTTP 404).
 * {@code partial} is true when at least one peer could not be contacted.
 */
public class RecordNotFoundException extends RuntimeException {

    private final boolean partial;

    public RecordNotFoundException(UUID id, boolean partial) {
        super("Maintenance record " + id + " not found" + (partial ? " on the reachable replicas" : ""));
        this.partial = partial;
    }

    public boolean isPartial() {
        return partial;
    }
}
