package pt.isep.aisafe.aircraft.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public class JsonErrorWriter {

    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public static void writeError(HttpServletResponse response, int status, String error, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(status, error, message, path);
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
