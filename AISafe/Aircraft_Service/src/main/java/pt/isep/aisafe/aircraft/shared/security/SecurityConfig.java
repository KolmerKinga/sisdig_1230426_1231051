package pt.isep.aisafe.aircraft.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import pt.isep.aisafe.aircraft.shared.audit.AuditFilter;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;
import pt.isep.aisafe.aircraft.shared.error.JsonErrorWriter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static pt.isep.aisafe.aircraft.shared.security.Role.ADMIN;
import static pt.isep.aisafe.aircraft.shared.security.Role.ATCC;
import static pt.isep.aisafe.aircraft.shared.security.Role.BACKOFFICE;
import static pt.isep.aisafe.aircraft.shared.security.Role.MAINTENANCE_SUPERVISOR;
import static pt.isep.aisafe.aircraft.shared.security.Role.MAINTENANCE_TECHNICIAN;

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

                        // --- Aircraft Endpoints ---
                        // US102 / US201: Registo de novas aeronaves
                        .requestMatchers(POST, "/api/aircraft").hasAnyRole(Role.names(BACKOFFICE, MAINTENANCE_SUPERVISOR, ADMIN))
                        // US105: Alteração de estado da aeronave
                        .requestMatchers(PATCH, "/api/aircraft/*/status").hasAnyRole(Role.names(BACKOFFICE, MAINTENANCE_SUPERVISOR, ADMIN))
                        // US103: Detalhes de aeronave por matrícula (também usado por rotas/voos)
                        .requestMatchers(GET, "/api/aircraft/*").hasAnyRole(Role.names(BACKOFFICE, ATCC, MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN))
                        // US104: Pesquisa e listagem de aeronaves
                        .requestMatchers(GET, "/api/aircraft").hasAnyRole(Role.names(BACKOFFICE, ATCC, MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN))

                        // --- Aircraft Model Endpoints ---
                        // Criar modelo no catálogo
                        .requestMatchers(POST, "/api/aircraft-models").hasAnyRole(Role.names(BACKOFFICE, ADMIN))
                        // Consultar detalhes do modelo
                        .requestMatchers(GET, "/api/aircraft-models/*").hasAnyRole(Role.names(BACKOFFICE, ATCC, MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN))
                        // Listar modelos do catálogo
                        .requestMatchers(GET, "/api/aircraft-models").hasAnyRole(Role.names(BACKOFFICE, ATCC, MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, ADMIN))

                        // tudo o resto é bloqueado
                        .anyRequest().denyAll())
                .addFilterAfter(new AuditFilter(properties.getInstanceName()), SecurityContextHolderFilter.class)
                .addFilterBefore(new ServiceKeyFilter(properties.getServiceKey(), errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), BasicAuthenticationFilter.class);

        return http.build();
    }
}
