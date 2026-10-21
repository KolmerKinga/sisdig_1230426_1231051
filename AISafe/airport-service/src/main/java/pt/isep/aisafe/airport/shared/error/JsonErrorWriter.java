package pt.isep.aisafe.airport.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import pt.isep.aisafe.airport.shared.config.AisafeProperties;

import java.io.IOException;
import java.util.List;

/**
 * Writes JSON error bodies from filters and Spring Security (401 / 403),
 * where the @RestControllerAdvice handler is not reached.
 */
@Component
public class JsonErrorWriter implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final AisafeProperties properties;

    public JsonErrorWriter(ObjectMapper objectMapper, AisafeProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), properties.instanceId(), List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /** 401 - no token, invalid signature or expired token. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "Missing, invalid or expired JWT");
    }

    /** 403 - valid token but the role is not allowed for this endpoint. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "Your role is not allowed to perform this operation");
    }
}
