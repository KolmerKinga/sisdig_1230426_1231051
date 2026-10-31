package pt.isep.aisafe.aircraft.shared.security;

import java.util.List;

public record AuthenticatedUser(
        String username,
        String email,
        List<String> roles
) {
    public AuthenticatedUser(String username, List<String> roles) {
        this(username, null, roles);
    }
}
