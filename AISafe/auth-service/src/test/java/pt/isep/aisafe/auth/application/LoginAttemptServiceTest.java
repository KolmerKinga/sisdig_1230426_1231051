package pt.isep.aisafe.auth.application;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    /** Clock that the test can move forward. */
    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-01T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void locksAfterMaxFailuresAndUnlocksAfterDuration() {
        MovableClock clock = new MovableClock();
        LoginAttemptService attempts = new LoginAttemptService(3, Duration.ofMinutes(5), clock);

        attempts.recordFailure("Ana");
        attempts.recordFailure("ana");
        assertThat(attempts.secondsLocked("ana")).isEqualTo(0L);

        attempts.recordFailure("ANA");
        assertThat(attempts.secondsLocked("ana")).isEqualTo(300L);

        clock.advance(Duration.ofMinutes(5));
        assertThat(attempts.secondsLocked("ana")).isEqualTo(0L);
    }

    @Test
    void successResetsTheCounter() {
        LoginAttemptService attempts = new LoginAttemptService(2, Duration.ofMinutes(5), new MovableClock());

        attempts.recordFailure("rui");
        attempts.recordSuccess("rui");
        attempts.recordFailure("rui");

        assertThat(attempts.secondsLocked("rui")).isEqualTo(0L);
    }
}
