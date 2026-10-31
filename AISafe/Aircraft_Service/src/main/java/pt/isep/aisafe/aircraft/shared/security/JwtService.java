package pt.isep.aisafe.aircraft.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class JwtService {

    private final AisafeProperties aisafeProperties;

    public JwtService(AisafeProperties aisafeProperties) {
        this.aisafeProperties = aisafeProperties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = aisafeProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthenticatedUser extractUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        String email = claims.get("email", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return new AuthenticatedUser(username, email, roles != null ? roles : List.of());
    }

    public AuthenticatedUser parse(String token) {
        return extractUser(token);
    }
}
