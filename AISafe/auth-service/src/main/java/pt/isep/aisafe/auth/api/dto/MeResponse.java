package pt.isep.aisafe.auth.api.dto;

import java.util.List;

/**
 * Answer of GET /auth/me: the identity carried by the token that was sent.
 */
public record MeResponse(String subject, String username, List<String> roles, String validatedBy) {
}
