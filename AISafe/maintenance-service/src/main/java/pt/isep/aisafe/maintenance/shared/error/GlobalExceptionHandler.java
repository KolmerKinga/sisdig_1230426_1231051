package pt.isep.aisafe.maintenance.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pt.isep.aisafe.maintenance.application.InvalidRequestException;
import pt.isep.aisafe.maintenance.application.RecordNotFoundException;
import pt.isep.aisafe.maintenance.application.TemplateNotFoundException;
import pt.isep.aisafe.maintenance.domain.InvalidMaintenanceException;
import pt.isep.aisafe.maintenance.domain.MaintenanceRuleException;
import pt.isep.aisafe.maintenance.infrastructure.client.AircraftNotFoundException;
import pt.isep.aisafe.maintenance.infrastructure.client.DependencyRejectedException;
import pt.isep.aisafe.maintenance.infrastructure.client.DependencyUnavailableException;
import pt.isep.aisafe.maintenance.shared.config.AisafeProperties;
import pt.isep.aisafe.maintenance.shared.forwarding.ForwardingHeaders;

import java.util.List;

/**
 * Maps exceptions to HTTP status codes with a JSON body (no stack traces are exposed).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AisafeProperties properties;

    public GlobalExceptionHandler(AisafeProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ErrorResponse> recordNotFound(RecordNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, ex.isPartial());
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> templateNotFound(TemplateNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, ex.isPartial());
    }

    @ExceptionHandler(AircraftNotFoundException.class)
    public ResponseEntity<ErrorResponse> aircraftNotFound(AircraftNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, false);
    }

    /** A service we depend on (aircraft-service) could not be reached: try again later. */
    @ExceptionHandler(DependencyUnavailableException.class)
    public ResponseEntity<ErrorResponse> dependencyDown(DependencyUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, null, true);
    }

    @ExceptionHandler(DependencyRejectedException.class)
    public ResponseEntity<ErrorResponse> dependencyRejected(DependencyRejectedException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, null, false);
    }

    @ExceptionHandler(MaintenanceRuleException.class)
    public ResponseEntity<ErrorResponse> conflict(MaintenanceRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null, false);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrentUpdate(ObjectOptimisticLockingFailureException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The record was changed by another request; try again", request, null, false);
    }

    @ExceptionHandler({InvalidMaintenanceException.class, InvalidRequestException.class})
    public ResponseEntity<ErrorResponse> badRequest(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Invalid input data", request, details, false);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> malformed(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request: check the body, ids, dates and enum values",
                request, null, false);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null, false);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> unsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request, null, false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, null, false);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
                                                List<String> details, boolean partial) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), properties.instanceId(), details);
        return ResponseEntity.status(status)
                .header(ForwardingHeaders.PARTIAL_RESULT, String.valueOf(partial))
                .body(body);
    }
}
