package pt.isep.aisafe.auth.domain;

import org.junit.jupiter.api.Test;
import pt.isep.aisafe.auth.shared.security.Role;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollaboratorTest {

    @Test
    void sameUsernameGetsSameIdOnEveryReplica() {
        Collaborator a = new Collaborator("Technician", "t@email.com", "$2a$hash", Set.of(Role.MAINTENANCE_TECHNICIAN));
        Collaborator b = new Collaborator("technician", "t@email.com", "$2a$other", Set.of(Role.MAINTENANCE_TECHNICIAN));

        assertThat(a.getId()).isEqualTo(b.getId());
        assertThat(a.getUsername()).isEqualTo("technician");
    }

    @Test
    void roleNamesAreSorted() {
        Collaborator c = new Collaborator("boss", "b@email.com", "$2a$hash", Set.of(Role.BACKOFFICE, Role.ADMIN));

        assertThat(c.roleNames()).isEqualTo(List.of("ADMIN", "BACKOFFICE"));
    }

    @Test
    void rejectsInvalidData() {
        assertThatThrownBy(() -> new Collaborator("a b", "x@email.com", "h", Set.of(Role.ATCC)))
                .isInstanceOf(InvalidCollaboratorException.class);
        assertThatThrownBy(() -> new Collaborator("valid", "not-an-email", "h", Set.of(Role.ATCC)))
                .isInstanceOf(InvalidCollaboratorException.class);
        assertThatThrownBy(() -> new Collaborator("valid", "v@email.com", "h", Set.of()))
                .isInstanceOf(InvalidCollaboratorException.class);
    }

    @Test
    void disabledCollaborator() {
        Collaborator c = new Collaborator("atcc", "atcc@email.com", "$2a$hash", Set.of(Role.ATCC));

        c.disable();

        assertThat(c.isEnabled()).isFalse();
    }
}
