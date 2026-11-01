package pt.isep.aisafe.auth.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pt.isep.aisafe.auth.application.AccountTemporarilyLockedException;
import pt.isep.aisafe.auth.application.InvalidCredentialsException;
import pt.isep.aisafe.auth.shared.config.AisafeProperties;

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

    /** Same message for unknown user and wrong password, so usernames cannot be discovered. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException ex,
                                                            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null));
    }

    /** Too many failed logins for this username: blocked for a few minutes. */
    @ExceptionHandler(AccountTemporarilyLockedException.class)
    public ResponseEntity<ErrorResponse> locked(AccountTemporarilyLockedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(body(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "Invalid input data", request, details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformed(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "Malformed request body", request, null));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(body(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, null));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> unsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.internalServerError()
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, null));
    }

    private ErrorResponse body(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        return ErrorResponse.of(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), properties.instanceId(), details);
    }
}
