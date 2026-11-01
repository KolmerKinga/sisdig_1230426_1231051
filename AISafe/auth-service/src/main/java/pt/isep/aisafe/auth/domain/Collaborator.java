package pt.isep.aisafe.auth.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pt.isep.aisafe.auth.shared.security.Role;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * A person who can log in to AISafe (Identity &amp; Access context).
 *
 * <p>The id is derived from the username, so both auth-service replicas give the same
 * collaborator the same id (the "sub" claim of the token does not depend on which replica
 * answered the login).</p>
 */
@Entity
@Table(name = "collaborators")
public class Collaborator {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    /** BCrypt hash, never the plain password. */
    @Column(nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collaborator_roles", joinColumns = @JoinColumn(name = "collaborator_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    @Column(nullable = false)
    private boolean enabled = true;

    protected Collaborator() {
        // for JPA
    }

    public Collaborator(String username, String email, String passwordHash, Set<Role> roles) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,50}")) {
            throw new InvalidCollaboratorException("Username must have 3-50 letters, digits, '.', '_' or '-'");
        }
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new InvalidCollaboratorException("A valid email is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidCollaboratorException("Password hash is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new InvalidCollaboratorException("At least one role is required");
        }
        this.username = username.toLowerCase(Locale.ROOT);
        this.id = idFor(this.username);
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = EnumSet.copyOf(roles);
    }

    /** Deterministic id: the same username always gets the same UUID on every replica. */
    public static UUID idFor(String username) {
        return UUID.nameUUIDFromBytes(("aisafe:" + username.toLowerCase(Locale.ROOT))
                .getBytes(StandardCharsets.UTF_8));
    }

    public void disable() {
        this.enabled = false;
    }

    /** Role names as they go into the "roles" claim of the token, sorted for stable output. */
    public List<String> roleNames() {
        return roles.stream().map(Enum::name).sorted().toList();
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
