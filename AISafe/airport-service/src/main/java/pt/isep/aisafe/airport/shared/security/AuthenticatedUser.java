package pt.isep.aisafe.airport.shared.security;

import java.util.List;

/**
 * Identity extracted from a valid JWT.
 *
 * @param subject  the "sub" claim (collaborator id in auth-service)
 * @param username the "username" claim, or the subject when absent
 * @param roles    the "roles" claim
 */
public record AuthenticatedUser(String subject, String username, List<String> roles) {
}
