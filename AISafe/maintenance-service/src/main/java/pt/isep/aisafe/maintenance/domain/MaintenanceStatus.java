package pt.isep.aisafe.maintenance.domain;

/**
 * Life cycle of a maintenance record (same names as PSOFT): INLINE (planned) -> ONGOING -> COMPLETED.
 */
public enum MaintenanceStatus {
    INLINE,
    ONGOING,
    COMPLETED
}
