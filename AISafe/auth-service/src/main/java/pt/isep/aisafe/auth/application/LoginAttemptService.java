package pt.isep.aisafe.auth.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection: after {@code maxFailures} wrong passwords for the same username,
 * that username is blocked for {@code lockDuration}. Kept in memory per replica.
 */
@Component
public class LoginAttemptService {

    private record Attempts(int failures, Instant lockedUntil) {
    }

    private final int maxFailures;
    private final Duration lockDuration;
    private final Clock clock;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(@Value("${aisafe.login.max-failures:5}") int maxFailures,
                               @Value("${aisafe.login.lock-duration:5m}") Duration lockDuration) {
        this(maxFailures, lockDuration, Clock.systemUTC());
    }

    public LoginAttemptService(int maxFailures, Duration lockDuration, Clock clock) {
        this.maxFailures = maxFailures;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    /** Seconds left in the block, or 0 when the username is not blocked. */
    public long secondsLocked(String username) {
        Attempts current = attempts.get(key(username));
        if (current == null || current.lockedUntil() == null) {
            return 0;
        }
        Instant now = clock.instant();
        if (!now.isBefore(current.lockedUntil())) {
            attempts.remove(key(username));
            return 0;
        }
        return Math.max(1, Duration.between(now, current.lockedUntil()).toSeconds());
    }

    public void recordFailure(String username) {
        attempts.compute(key(username), (k, current) -> {
            int failures = (current == null ? 0 : current.failures()) + 1;
            Instant lockedUntil = failures >= maxFailures ? clock.instant().plus(lockDuration) : null;
            return new Attempts(failures, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
