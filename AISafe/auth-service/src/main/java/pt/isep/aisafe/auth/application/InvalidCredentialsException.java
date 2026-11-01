package pt.isep.aisafe.auth.application;

/**
 * Wrong username or password (HTTP 401). The message is the same in both cases on purpose,
 * so an attacker cannot find out which usernames exist.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
