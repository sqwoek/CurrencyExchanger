package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.ValidationException;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.service.CurrencyService;
import roadmap.validator.CurrencyValidator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

@WebServlet("/api/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.currencyService = (CurrencyService) context.getAttribute("currencyService");
        this.objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        String code = path.substring(1);

        try {
            CurrencyValidator.validateCode(code);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }

        try {
            CurrencyResponseDto currency = currencyService.get(code);
            String jsonResponse = objectMapper.writeValueAsString(currency);
            resp.getWriter().write(jsonResponse);
        } catch (NoSuchElementException ex) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(ex.getMessage());
        } catch (DatabaseException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(ex.getMessage());
        }
    }
}