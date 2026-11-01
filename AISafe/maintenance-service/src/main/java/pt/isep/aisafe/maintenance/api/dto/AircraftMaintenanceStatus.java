package pt.isep.aisafe.maintenance.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Answer of GET /api/maintenance/aircraft/{registration}/ongoing, used by route-flight-service
 * before scheduling a flight.
 *
 * @param underMaintenance true if any replica has an ONGOING record for the aircraft
 * @param ongoingRecordIds ids of those records
 * @param complete         false if a replica could not be asked (the answer may be incomplete)
 */
public record AircraftMaintenanceStatus(String aircraftRegistration, boolean underMaintenance,
                                        List<UUID> ongoingRecordIds, boolean complete) {
}
