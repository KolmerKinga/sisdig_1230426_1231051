package pt.isep.aisafe.auth.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Replica-specific settings, bound from the "aisafe.*" properties
 * (application.properties + application-instanceN.properties or environment variables).
 *
 * <p>Same structure as in the other services. Differences in auth-service: the JWT settings also
 * have the token lifetime (this service issues tokens), and {@code peers} is not used because
 * both replicas hold the same collaborators (no forwarding needed).</p>
 *
 * @param instanceId  name of this replica, e.g. auth-1 (used in headers and logs)
 * @param peers       base URLs of the other replicas (kept for symmetry, not used)
 * @param serviceKey  shared secret required on every internal (service-to-service) call
 * @param jwt         JWT signing settings
 * @param forwarding  not used by auth-service (kept so the shared classes stay identical)
 */
@ConfigurationProperties(prefix = "aisafe")
public record AisafeProperties(
        String instanceId,
        List<String> peers,
        String serviceKey,
        Jwt jwt,
        Forwarding forwarding) {

    public AisafeProperties {
        peers = peers == null ? List.of()
                : peers.stream().filter(p -> p != null && !p.isBlank()).toList();
        if (jwt == null) {
            jwt = new Jwt(null, null, null);
        }
        if (forwarding == null) {
            forwarding = new Forwarding(null, null, 0, null, null);
        }
    }

    /**
     * @param secret     Base64 HMAC key shared with every service (at least 256 bits)
     * @param expiration lifetime of the issued tokens (default 1 hour)
     * @param issuer     value of the "iss" claim
     */
    public record Jwt(String secret, Duration expiration, String issuer) {
        public Jwt {
            expiration = expiration == null ? Duration.ofHours(1) : expiration;
            issuer = issuer == null || issuer.isBlank() ? "aisafe-auth-service" : issuer;
        }
    }

    public record Forwarding(Duration connectTimeout, Duration readTimeout, int attempts,
                             Duration backoff, Duration peerDownFor) {
        public Forwarding {
            connectTimeout = connectTimeout == null ? Duration.ofSeconds(1) : connectTimeout;
            readTimeout = readTimeout == null ? Duration.ofSeconds(2) : readTimeout;
            attempts = attempts <= 0 ? 2 : attempts;
            backoff = backoff == null ? Duration.ofMillis(200) : backoff;
            peerDownFor = peerDownFor == null ? Duration.ofSeconds(10) : peerDownFor;
        }
    }
}
