package pt.isep.aisafe.airport.shared.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isep.aisafe.airport.shared.forwarding.ForwardingHeaders;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Audit logging and traceability.
 *
 * <p>Assigns a correlation id (or reuses the one received from another replica/service),
 * adds X-Served-By / X-Correlation-Id to every response and writes one line per API call
 * to the AUDIT logger (file logs/audit-&lt;instance&gt;.log): who, what, result, which replica.</p>
 *
 * <p>Registered inside the security chain right after the SecurityContext is created, so
 * it also records requests rejected with 401/403.</p>
 */
public class AuditFilter extends OncePerRequestFilter {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private static final String MDC_KEY = "correlationId";

    private final String instanceId;

    public AuditFilter(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String correlationId = request.getHeader(ForwardingHeaders.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank() || correlationId.length() > 100) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setAttribute(ForwardingHeaders.CORRELATION_ATTRIBUTE, correlationId);
        response.setHeader(ForwardingHeaders.CORRELATION_ID, correlationId);
        response.setHeader(ForwardingHeaders.SERVED_BY, instanceId);
        MDC.put(MDC_KEY, correlationId);

        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean anonymous = authentication == null || authentication instanceof AnonymousAuthenticationToken;
                String user = anonymous ? "anonymous" : authentication.getName();
                String roles = anonymous ? "-" : authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                String forwardedBy = request.getHeader(ForwardingHeaders.FORWARDED_BY);
                AUDIT.info("instance={} user={} roles={} action={} {} status={} forwardedBy={} internal={} durationMs={}",
                        instanceId, user, roles, request.getMethod(), ForwardingHeaders.pathAndQuery(request),
                        response.getStatus(), forwardedBy == null ? "-" : forwardedBy,
                        request.getAttribute(ForwardingHeaders.INTERNAL_CALL_ATTRIBUTE) != null,
                        (System.nanoTime() - start) / 1_000_000);
            }
            MDC.remove(MDC_KEY);
        }
    }
}
