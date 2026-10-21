package pt.isep.aisafe.airport.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isep.aisafe.airport.shared.error.JsonErrorWriter;
import pt.isep.aisafe.airport.shared.forwarding.ForwardingHeaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Inter-service authentication.
 *
 * <p>A request that claims to be internal (has X-Forwarded-By or X-Service-Key) must carry the
 * correct service key; otherwise it is rejected with 403. This prevents an external client from
 * faking a forwarded request to bypass peer forwarding rules.</p>
 *
 * <p>The user's JWT is still required and checked on internal calls, so role-based access
 * control is enforced on every replica.</p>
 */
public class ServiceKeyFilter extends OncePerRequestFilter {

    private final byte[] expectedKey;
    private final JsonErrorWriter errorWriter;

    public ServiceKeyFilter(String serviceKey, JsonErrorWriter errorWriter) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("aisafe.service-key (AISAFE_SERVICE_KEY) must be configured");
        }
        this.expectedKey = serviceKey.getBytes(StandardCharsets.UTF_8);
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String forwardedBy = request.getHeader(ForwardingHeaders.FORWARDED_BY);
        String key = request.getHeader(ForwardingHeaders.SERVICE_KEY);

        if (forwardedBy != null || key != null) {
            boolean valid = key != null
                    && MessageDigest.isEqual(expectedKey, key.getBytes(StandardCharsets.UTF_8));
            if (!valid) {
                errorWriter.write(request, response, HttpStatus.FORBIDDEN,
                        "Internal call rejected: missing or invalid service key");
                return;
            }
            request.setAttribute(ForwardingHeaders.INTERNAL_CALL_ATTRIBUTE, Boolean.TRUE);
        }
        chain.doFilter(request, response);
    }
}
