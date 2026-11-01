package pt.isep.aisafe.auth.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Issues and validates JWTs.
 *
 * <p>auth-service is the only service that ISSUES tokens. Every other service only validates them
 * with the same shared key (AISAFE_JWT_SECRET), so they never call auth-service per request.</p>
 *
 * <p>Token contract (Published Language, used by all AISafe services):</p>
 * <ul>
 *   <li>{@code sub} - collaborator id</li>
 *   <li>{@code username} - login name (used in audit logs)</li>
 *   <li>{@code roles} - list of role names (ADMIN, ATCC, BACKOFFICE, MAINTENANCE_TECHNICIAN,
 *       MAINTENANCE_SUPERVISOR)</li>
 *   <li>{@code iss}, {@code iat}, {@code exp}; signed with HMAC-SHA256</li>
 * </ul>
 */
@Component
public class JwtService {

    /** A signed token plus its lifetime, returned by the login use case. */
    public record IssuedToken(String token, long expiresInSeconds) {
    }

    private final SecretKey key;
    private final Duration expiration;
    private final String issuer;

    public JwtService(AisafeProperties properties) {
        String secret = properties.jwt().secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("aisafe.jwt.secret (AISAFE_JWT_SECRET) must be configured");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = properties.jwt().expiration();
        this.issuer = properties.jwt().issuer();
    }

    public IssuedToken issue(String subject, String username, List<String> roles) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(subject)
                .claim("username", username)
                .claim("roles", roles)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiration.toSeconds());
    }

    /** Returns the user when the token has a valid signature and is not expired. */
    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            Object usernameClaim = claims.get("username");
            String username = usernameClaim == null ? subject : usernameClaim.toString();
            Object rolesClaim = claims.get("roles");
            List<String> roles = rolesClaim instanceof List<?> list
                    ? list.stream().map(Object::toString).toList()
                    : List.of();
            return Optional.of(new AuthenticatedUser(subject, username, roles));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
