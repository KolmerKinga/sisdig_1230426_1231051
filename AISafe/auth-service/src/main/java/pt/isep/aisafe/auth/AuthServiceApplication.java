package pt.isep.aisafe.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;

/**
 * Identity &amp; Access service (SIDIS Assignment 1): login and JWT issuing.
 *
 * <p>Run one process per replica, e.g.:
 * {@code mvn spring-boot:run -Dspring-boot.run.profiles=instance1} (https://localhost:8051) and
 * {@code mvn spring-boot:run -Dspring-boot.run.profiles=instance2} (https://localhost:8052).</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(AisafeProperties.class)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
