package pt.isep.aisafe.maintenance.application;

import java.util.UUID;

/**
 * The template is not on this replica nor on any reachable peer (HTTP 404).
 */
public class TemplateNotFoundException extends RuntimeException {

    private final boolean partial;

    public TemplateNotFoundException(UUID id, boolean partial) {
        super("Maintenance template " + id + " not found" + (partial ? " on the reachable replicas" : ""));
        this.partial = partial;
    }

    public boolean isPartial() {
        return partial;
    }
}
