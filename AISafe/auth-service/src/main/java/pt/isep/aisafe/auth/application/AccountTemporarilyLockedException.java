package pt.isep.aisafe.auth.application;

/**
 * Too many failed logins for the same username (HTTP 429 with Retry-After).
 */
public class AccountTemporarilyLockedException extends RuntimeException {

    private final long retryAfterSeconds;

    public AccountTemporarilyLockedException(long retryAfterSeconds) {
        super("Too many failed login attempts. Try again in " + retryAfterSeconds + " seconds");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
