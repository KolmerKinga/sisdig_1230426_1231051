package pt.isep.aisafe.airport.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import pt.isep.aisafe.airport.shared.audit.AuditFilter;
import pt.isep.aisafe.airport.shared.config.AisafeProperties;
import pt.isep.aisafe.airport.shared.error.JsonErrorWriter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static pt.isep.aisafe.airport.shared.security.Role.ADMIN;
import static pt.isep.aisafe.airport.shared.security.Role.ATCC;
import static pt.isep.aisafe.airport.shared.security.Role.BACKOFFICE;

/**
 * Stateless security: HTTPS (server.ssl), JWT authentication, service key for internal calls,
 * role-based access control per endpoint and audit logging.
 *
 * <p>Filter order inside the chain: AuditFilter -> ServiceKeyFilter -> JwtAuthenticationFilter
 * -> authorization rules below.</p>
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

                        // US106 / US207 register, US225 CSV import
                        .requestMatchers(POST, "/api/airports").hasAnyRole(Role.names(BACKOFFICE, ADMIN))
                        .requestMatchers(POST, "/api/airports/import").hasAnyRole(Role.names(BACKOFFICE, ADMIN))
                        // US106a add certification
                        .requestMatchers(POST, "/api/airports/*/certifications")
                        .hasAnyRole(Role.names(BACKOFFICE, ATCC, ADMIN))
                        // US109 status, US208 details
                        .requestMatchers(PATCH, "/api/airports/*/status").hasAnyRole(Role.names(BACKOFFICE, ADMIN))
                        .requestMatchers(PATCH, "/api/airports/*/details").hasAnyRole(Role.names(BACKOFFICE, ADMIN))
                        // US211 grouped (must come before /api/airports/*)
                        .requestMatchers(GET, "/api/airports/grouped").hasAnyRole(Role.names(ATCC, ADMIN))
                        // US107 details by IATA (also used by route-flight-service)
                        .requestMatchers(GET, "/api/airports/*").hasAnyRole(Role.names(BACKOFFICE, ATCC, ADMIN))
                        // US108 search
                        .requestMatchers(GET, "/api/airports").hasAnyRole(Role.names(ATCC, ADMIN))

                        // everything else is closed
                        .anyRequest().denyAll())
                .addFilterAfter(new AuditFilter(properties.instanceId()), SecurityContextHolderFilter.class)
                .addFilterBefore(new ServiceKeyFilter(properties.serviceKey(), errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), BasicAuthenticationFilter.class);

        return http.build();
    }
}
