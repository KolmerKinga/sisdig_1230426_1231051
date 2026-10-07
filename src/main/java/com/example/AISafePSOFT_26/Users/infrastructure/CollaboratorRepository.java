package com.example.AISafePSOFT_26.Users.infrastructure;

import com.example.AISafePSOFT_26.Users.domain.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    Optional<Collaborator> findByEmail(String email);
    Optional<Collaborator> findByUsername(String username);
}
