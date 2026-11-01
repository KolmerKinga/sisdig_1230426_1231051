package pt.isep.aisafe.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.auth.domain.Collaborator;
import pt.isep.aisafe.auth.infrastructure.CollaboratorRepository;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;
import pt.isep.aisafe.auth.shared.security.JwtService;
import pt.isep.aisafe.auth.shared.security.JwtService.IssuedToken;

import java.util.Locale;
import java.util.Optional;

/**
 * Login use case: checks the credentials and issues a signed JWT.
 *
 * <ul>
 *   <li>Passwords are compared with BCrypt; the plain password is never logged.</li>
 *   <li>Unknown user and wrong password give the same answer (401), and an unknown user still
 *       runs a BCrypt comparison, so response times do not reveal which usernames exist.</li>
 *   <li>Too many failures for a username block it temporarily (429).</li>
 *   <li>Every attempt is written to the AUDIT log.</li>
 * </ul>
 */
@Service
public class LoginService {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Result of a successful login. */
    public record LoginResult(Collaborator collaborator, IssuedToken token) {
    }

    private final CollaboratorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;
    private final String instanceId;
    private final String dummyHash;

    public LoginService(CollaboratorRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        LoginAttemptService attempts, AisafeProperties properties) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
        this.instanceId = properties.instanceId();
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Transactional(readOnly = true)
    public LoginResult login(String username, String password) {
        String normalized = username.trim().toLowerCase(Locale.ROOT);

        long lockedFor = attempts.secondsLocked(normalized);
        if (lockedFor > 0) {
            AUDIT.warn("instance={} event=LOGIN_BLOCKED user={} retryAfterSeconds={}", instanceId, normalized, lockedFor);
            throw new AccountTemporarilyLockedException(lockedFor);
        }

        Optional<Collaborator> found = repository.findByUsername(normalized);
        boolean passwordMatches = passwordEncoder.matches(password,
                found.map(Collaborator::getPasswordHash).orElse(dummyHash));

        if (found.isEmpty() || !found.get().isEnabled() || !passwordMatches) {
            attempts.recordFailure(normalized);
            AUDIT.warn("instance={} event=LOGIN_FAILED user={}", instanceId, normalized);
            throw new InvalidCredentialsException();
        }

        Collaborator collaborator = found.get();
        attempts.recordSuccess(normalized);
        IssuedToken token = jwtService.issue(collaborator.getId().toString(), collaborator.getUsername(),
                collaborator.roleNames());
        AUDIT.info("instance={} event=LOGIN_SUCCESS user={} roles={}", instanceId, collaborator.getUsername(),
                String.join(",", collaborator.roleNames()));
        return new LoginResult(collaborator, token);
    }
}
