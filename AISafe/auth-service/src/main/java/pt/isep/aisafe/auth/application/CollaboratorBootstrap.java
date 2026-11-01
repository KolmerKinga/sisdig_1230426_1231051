package pt.isep.aisafe.auth.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.aisafe.auth.domain.Collaborator;
import pt.isep.aisafe.auth.infrastructure.CollaboratorRepository;
import pt.isep.aisafe.auth.shared.security.Role;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;

/**
 * Loads the collaborators (same users as the PSOFT bootstrap) when the replica starts.
 *
 * <p>Both replicas load the SAME file, so either one can answer a login: users are reference
 * data replicated on every replica, which is why auth-service needs no peer forwarding.
 * Passwords in the seed file are development values; they are hashed with BCrypt before
 * being stored.</p>
 */
@Component
public class CollaboratorBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CollaboratorBootstrap.class);

    /** One entry of seed/collaborators.json. */
    public record SeedCollaborator(String username, String email, String password, List<Role> roles) {
    }

    private final CollaboratorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String seedFile;

    public CollaboratorBootstrap(CollaboratorRepository repository, PasswordEncoder passwordEncoder,
                                 ObjectMapper objectMapper, ResourceLoader resourceLoader,
                                 @Value("${aisafe.bootstrap.file:}") String seedFile) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.seedFile = seedFile;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (seedFile == null || seedFile.isBlank() || repository.count() > 0) {
            return;
        }
        Resource resource = resourceLoader.getResource(seedFile);
        if (!resource.exists()) {
            log.warn("Seed file {} not found; no collaborators loaded", seedFile);
            return;
        }
        try (InputStream input = resource.getInputStream()) {
            List<SeedCollaborator> seed = objectMapper.readValue(input,
                    new TypeReference<List<SeedCollaborator>>() {
                    });
            for (SeedCollaborator c : seed) {
                repository.save(new Collaborator(c.username(), c.email(),
                        passwordEncoder.encode(c.password()), EnumSet.copyOf(c.roles())));
            }
            log.info("Bootstrap: {} collaborator(s) loaded from {}", seed.size(), seedFile);
        }
    }
}
