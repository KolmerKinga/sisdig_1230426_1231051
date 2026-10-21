package pt.isep.aisafe.airport.shared.security;

import java.util.Arrays;

/**
 * Roles carried in the JWT "roles" claim (same names as in PSOFT).
 */
public enum Role {
    ADMIN,
    ATCC,
    BACKOFFICE,
    MAINTENANCE_TECHNICIAN,
    MAINTENANCE_SUPERVISOR;

    /** Role names for Spring Security's hasAnyRole(...). */
    public static String[] names(Role... roles) {
        return Arrays.stream(roles).map(Enum::name).toArray(String[]::new);
    }
}
