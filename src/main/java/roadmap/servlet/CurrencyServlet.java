package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.ConnectionManager;
import roadmap.dao.CurrencyDao;
import roadmap.model.dto.CurrencyDto;
import roadmap.service.CurrencyService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

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
        CurrencyDto currency = currencyService.get(code);

        String jsonResponse = objectMapper.writeValueAsString(currency);
        resp.getWriter().write(jsonResponse);
    }
}