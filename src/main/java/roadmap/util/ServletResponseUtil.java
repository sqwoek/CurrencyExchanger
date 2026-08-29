package roadmap.util;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ServletResponseUtil {
    private static ObjectMapper objectMapper;

    private ServletResponseUtil() {
    }

    public static void init(ObjectMapper mapper) {
        if (objectMapper == null) {
            objectMapper = mapper;
        } else {
            throw new IllegalStateException("ObjectMapper already initialized.");
        }
    }

    public static void sendSuccessResponse(HttpServletResponse resp, int successCode, Object responseDto) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(successCode);

        String jsonResponse = objectMapper.writeValueAsString(responseDto);
        resp.getWriter().write(jsonResponse);
    }

    public static void sendErrorResponse(HttpServletResponse resp, int errorCode, String errorMessage) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(errorCode);
        Map<String, String> error = new HashMap<>();
        error.put("message", errorMessage);

        String jsonError = objectMapper.writeValueAsString(error);
        resp.getWriter().write(jsonError);
    }
}