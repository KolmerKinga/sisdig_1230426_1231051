package pt.isep.aisafe.airport.shared.forwarding;

import java.util.List;

/**
 * Items collected from all reachable peers for an aggregated query.
 *
 * @param items   items returned by the peers (may contain duplicates; the caller merges them)
 * @param partial true if at least one peer could not be contacted
 */
public record PeerCollection<T>(List<T> items, boolean partial) {
}
