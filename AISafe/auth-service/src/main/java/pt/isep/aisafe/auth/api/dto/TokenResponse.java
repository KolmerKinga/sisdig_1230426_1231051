package pt.isep.aisafe.auth.api.dto;

import java.util.List;

/**
 * Answer of a successful login. Send {@code token} in "Authorization: Bearer &lt;token&gt;"
 * to any AISafe service.
 */
public record TokenResponse(String token, String tokenType, long expiresIn, String username, List<String> roles) {
}
