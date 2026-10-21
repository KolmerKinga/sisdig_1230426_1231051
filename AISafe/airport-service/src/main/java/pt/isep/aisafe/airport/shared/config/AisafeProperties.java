package pt.isep.aisafe.airport.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Replica-specific settings, bound from the "aisafe.*" properties
 * (application.properties + application-instanceN.properties or environment variables).
 *
 * @param instanceId  name of this replica, e.g. airport-1 (used in headers and logs)
 * @param peers       base URLs of the OTHER replicas of this service (never this replica)
 * @param serviceKey  shared secret required on every internal (service-to-service) call
 * @param jwt         JWT validation settings
 * @param forwarding  timeouts, retries and circuit breaker for peer calls
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
                : peers.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(p -> p.endsWith("/") ? p.substring(0, p.length() - 1) : p)
                .toList();
        if (forwarding == null) {
            forwarding = new Forwarding(null, null, 0, null, null);
        }
    }

    /**
     * @param secret Base64 HMAC key shared with auth-service (at least 256 bits)
     */
    public record Jwt(String secret) {
    }

    /**
     * @param connectTimeout max time to open a connection to a peer
     * @param readTimeout    max time waiting for a peer answer
     * @param attempts       attempts per peer before giving up (1 = no retry)
     * @param backoff        wait before the second attempt (doubles on each retry)
     * @param peerDownFor    how long a failing peer is skipped (circuit breaker)
     */
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
