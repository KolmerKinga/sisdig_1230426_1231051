package pt.isep.aisafe.airport.api.dto;

import java.util.List;

/**
 * Result of POST /api/airports/import (PSOFT US225).
 *
 * @param imported airports stored on this replica
 * @param skipped  one line per rejected row, with the reason
 * @param partial  true if a peer could not be checked for duplicates
 */
public record ImportReport(int imported, List<String> skipped, boolean partial) {
}
