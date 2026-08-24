package roadmap.util;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

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

    public static void sendSuccessResponse(HttpServletResponse resp, Object responseDto) throws IOException {
        resp.setContentType("application/json");
        String jsonResponse = getJsonResponse(responseDto);
        resp.getWriter().write(jsonResponse);
    }

    public static void sendErrorResponse(HttpServletResponse resp, int errorCode, String errorMessage) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(errorCode);
        resp.getWriter().write(errorMessage);
    }


    private static String getJsonResponse(Object responseDto) {
        return objectMapper.writeValueAsString(responseDto);
    }
}