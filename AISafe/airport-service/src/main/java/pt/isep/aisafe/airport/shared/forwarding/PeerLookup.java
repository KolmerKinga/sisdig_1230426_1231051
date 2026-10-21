package pt.isep.aisafe.airport.shared.forwarding;

import java.util.Optional;

/**
 * Result of looking for a single resource on the peers.
 *
 * @param response the first peer answer that was not 404 (empty if every reachable peer said 404)
 * @param partial  true if at least one peer could not be contacted
 */
public record PeerLookup(Optional<PeerResponse> response, boolean partial) {

    /** True when a peer holds the resource (2xx answer). */
    public boolean found() {
        return response.isPresent() && response.get().isSuccess();
    }
}
