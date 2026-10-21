package pt.isep.aisafe.airport.shared.error;

import java.time.Instant;
import java.util.List;

/**
 * Error body returned by every endpoint. Never contains stack traces.
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String instance,
        List<String> details) {

    public static ErrorResponse of(int status, String error, String message, String path,
                                   String instance, List<String> details) {
        return new ErrorResponse(Instant.now().toString(), status, error, message, path, instance,
                details == null ? List.of() : details);
    }
}
