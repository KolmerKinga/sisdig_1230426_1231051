package pt.isep.aisafe.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pt.isep.aisafe.maintenance.shared.config.AisafeProperties;

/**
 * Maintenance Management service (SIDIS Assignment 1).
 *
 * <p>Run one process per replica, e.g.:
 * {@code mvn spring-boot:run -Dspring-boot.run.profiles=instance1} (https://localhost:8061) and
 * {@code mvn spring-boot:run -Dspring-boot.run.profiles=instance2} (https://localhost:8062).</p>
 *
 * <p>Users are not managed here (JWTs come from auth-service), so the default in-memory user
 * store of Spring Boot is disabled.</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(AisafeProperties.class)
public class MaintenanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaintenanceServiceApplication.class, args);
    }
}
