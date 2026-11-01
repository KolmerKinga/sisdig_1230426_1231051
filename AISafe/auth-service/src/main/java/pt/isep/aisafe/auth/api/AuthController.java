package pt.isep.aisafe.auth.api;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.aisafe.auth.api.dto.LoginRequest;
import pt.isep.aisafe.auth.api.dto.MeResponse;
import pt.isep.aisafe.auth.api.dto.TokenResponse;
import pt.isep.aisafe.auth.application.LoginService;
import pt.isep.aisafe.auth.application.LoginService.LoginResult;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;
import pt.isep.aisafe.auth.shared.security.AuthenticatedUser;

import java.util.List;

/**
 * Identity &amp; Access API.
 *
 * <ul>
 *   <li>POST /auth/login - public; returns a JWT valid in every AISafe service.</li>
 *   <li>GET /auth/me - requires a token; shows the identity inside it.</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final String instanceId;

    public AuthController(LoginService loginService, AisafeProperties properties) {
        this.loginService = loginService;
        this.instanceId = properties.instanceId();
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginService.login(request.username(), request.password());
        return new TokenResponse(
                result.token().token(),
                "Bearer",
                result.token().expiresInSeconds(),
                result.collaborator().getUsername(),
                result.collaborator().roleNames());
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .sorted()
                .toList();
        String subject = authentication.getDetails() instanceof AuthenticatedUser user
                ? user.subject()
                : authentication.getName();
        return new MeResponse(subject, authentication.getName(), roles, instanceId);
    }
}
