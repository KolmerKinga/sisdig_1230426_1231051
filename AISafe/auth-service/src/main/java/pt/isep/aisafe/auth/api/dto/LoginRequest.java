package pt.isep.aisafe.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /auth/login.
 */
public record LoginRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 100) String password) {

    /** Never print the password (e.g. in logs or debugger output). */
    @Override
    public String toString() {
        return "LoginRequest[username=" + username + ", password=***]";
    }
}
