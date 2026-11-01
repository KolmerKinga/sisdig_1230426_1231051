package pt.isep.aisafe.auth.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import pt.isep.aisafe.auth.shared.audit.AuditFilter;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;
import pt.isep.aisafe.auth.shared.error.JsonErrorWriter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Stateless security for auth-service: HTTPS (server.ssl), public login endpoint,
 * JWT authentication for /auth/me, service key for internal calls and audit logging.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   AisafeProperties properties,
                                                   JsonErrorWriter errorWriter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorWriter)
                        .accessDeniedHandler(errorWriter))
                .authorizeHttpRequests(auth -> auth
                        // infrastructure
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        // login is public: it is how a client obtains a token
                        .requestMatchers(POST, "/auth/login").permitAll()
                        // who am I (proves the token is valid)
                        .requestMatchers(GET, "/auth/me").authenticated()
                        // everything else is closed
                        .anyRequest().denyAll())
                .addFilterAfter(new AuditFilter(properties.instanceId()), SecurityContextHolderFilter.class)
                .addFilterBefore(new ServiceKeyFilter(properties.serviceKey(), errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), BasicAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt hashes for collaborator passwords (never stored in plain text). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
