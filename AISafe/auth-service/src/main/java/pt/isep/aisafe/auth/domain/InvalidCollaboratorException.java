package pt.isep.aisafe.auth.domain;

/**
 * Raised when collaborator data breaks a rule (used when loading the seed data).
 */
public class InvalidCollaboratorException extends RuntimeException {
    public InvalidCollaboratorException(String message) {
        super(message);
    }
}
