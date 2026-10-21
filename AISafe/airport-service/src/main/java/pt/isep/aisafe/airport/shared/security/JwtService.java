package pt.isep.aisafe.airport.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.airport.shared.config.AisafeProperties;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

/**
 * Validates JWTs issued by auth-service. This service never issues tokens.
 * Validation is local (shared HMAC key), so airport-service keeps working
 * even if auth-service is down.
 */
@Component
public class JwtService {

    private final SecretKey key;

    public JwtService(AisafeProperties properties) {
        if (properties.jwt() == null || properties.jwt().secret() == null || properties.jwt().secret().isBlank()) {
            throw new IllegalStateException("aisafe.jwt.secret (AISAFE_JWT_SECRET) must be configured");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.jwt().secret()));
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
