package pt.isep.aisafe.aircraft.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isep.aisafe.aircraft.shared.config.AisafeProperties;
import pt.isep.aisafe.aircraft.shared.error.JsonErrorWriter;
import pt.isep.aisafe.aircraft.shared.forwarding.ForwardingHeaders;

import java.io.IOException;

@Component
public class ServiceKeyFilter extends OncePerRequestFilter {

    private final AisafeProperties aisafeProperties;

    public ServiceKeyFilter(AisafeProperties aisafeProperties) {
        this.aisafeProperties = aisafeProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String forwardedBy = request.getHeader(ForwardingHeaders.FORWARDED_BY);
        String serviceKey = request.getHeader(ForwardingHeaders.SERVICE_KEY);

        if (forwardedBy != null) {
            if (serviceKey == null || !serviceKey.equals(aisafeProperties.getServiceKey())) {
                JsonErrorWriter.writeError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Invalid Service Key");
                return;
            }
            request.setAttribute(ForwardingHeaders.INTERNAL_CALL_ATTRIBUTE, true);
        }

        filterChain.doFilter(request, response);
    }
}
