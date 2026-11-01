package pt.isep.aisafe.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.aisafe.auth.domain.Collaborator;

import java.util.Optional;
import java.util.UUID;

public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {

    Optional<Collaborator> findByUsername(String username);
}
