package pt.isep.aisafe.maintenance.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import pt.isep.aisafe.maintenance.shared.audit.AuditFilter;
import pt.isep.aisafe.maintenance.shared.config.AisafeProperties;
import pt.isep.aisafe.maintenance.shared.error.JsonErrorWriter;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static pt.isep.aisafe.maintenance.shared.security.Role.ADMIN;
import static pt.isep.aisafe.maintenance.shared.security.Role.ATCC;
import static pt.isep.aisafe.maintenance.shared.security.Role.MAINTENANCE_SUPERVISOR;
import static pt.isep.aisafe.maintenance.shared.security.Role.MAINTENANCE_TECHNICIAN;

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

    private static final String BASE = "/api/maintenance";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   AisafeProperties properties,
                                                   JsonErrorWriter errorWriter) throws Exception {
        String[] technicianSupervisorAdmin = Role.names(MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN);

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

                        // peer-to-peer aggregation: only replicas of this service (valid service key)
                        .requestMatchers(GET, BASE + "/internal/**").access(internalCallsOnly())

                        // US115 templates
                        .requestMatchers(POST, BASE + "/templates").hasAnyRole(Role.names(MAINTENANCE_TECHNICIAN, ADMIN))
                        .requestMatchers(GET, BASE + "/templates", BASE + "/templates/*").hasAnyRole(technicianSupervisorAdmin)

                        // US115 create record
                        .requestMatchers(POST, BASE + "/records", BASE + "/record")
                        .hasAnyRole(Role.names(MAINTENANCE_TECHNICIAN, ADMIN))
                        // US119 start / complete, parts, US217 attribute
                        .requestMatchers(PATCH, BASE + "/records/*", BASE + "/records/*/status").hasAnyRole(technicianSupervisorAdmin)
                        .requestMatchers(POST, BASE + "/records/*/complete", BASE + "/records/*/parts")
                        .hasAnyRole(technicianSupervisorAdmin)
                        // US116 / US218 search, record by id
                        .requestMatchers(GET, BASE + "/records", BASE + "/records/*").hasAnyRole(technicianSupervisorAdmin)

                        // US219 ongoing activities
                        .requestMatchers(GET, BASE + "/ongoing").hasAnyRole(technicianSupervisorAdmin)
                        // US221 turnaround
                        .requestMatchers(GET, BASE + "/turnaround-average")
                        .hasAnyRole(Role.names(MAINTENANCE_SUPERVISOR, ADMIN))
                        // US220 costs, US117 hours, US222 alerts
                        .requestMatchers(GET, BASE + "/reports/**", BASE + "/alerts")
                        .hasAnyRole(Role.names(ATCC, MAINTENANCE_SUPERVISOR, ADMIN))
                        // US226 parts inventory and low stock
                        .requestMatchers(GET, BASE + "/parts/**").hasAnyRole(Role.names(MAINTENANCE_SUPERVISOR, ADMIN))
                        // contract for route-flight-service (called with the ATCC's token)
                        .requestMatchers(GET, BASE + "/aircraft/*/ongoing")
                        .hasAnyRole(Role.names(ATCC, MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN))

                        // everything else is closed
                        .anyRequest().denyAll())
                .addFilterAfter(new AuditFilter(properties.instanceId()), SecurityContextHolderFilter.class)
                .addFilterBefore(new ServiceKeyFilter(properties.serviceKey(), errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), BasicAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Allows the request only when ServiceKeyFilter validated the X-Service-Key header,
     * i.e. the caller is another AISafe replica, not an external client.
     */
    private static AuthorizationManager<RequestAuthorizationContext> internalCallsOnly() {
        return (authentication, context) -> new AuthorizationDecision(
                Boolean.TRUE.equals(context.getRequest().getAttribute(ForwardingHeaders.INTERNAL_CALL_ATTRIBUTE)));
    }
}
