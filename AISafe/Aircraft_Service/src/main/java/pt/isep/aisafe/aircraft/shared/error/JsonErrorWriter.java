package pt.isep.aisafe.aircraft.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonErrorWriter {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private JsonErrorWriter() {}

    public static void writeError(HttpServletResponse response, int status, String error, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(status, error, message, path);
        mapper.writeValue(response.getWriter(), body);
    }

    public static void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        writeError(response, status, error, message, null);
    }
}
