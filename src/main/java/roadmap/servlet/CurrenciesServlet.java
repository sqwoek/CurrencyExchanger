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
import java.util.List;

@WebServlet("/api/currencies/*")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.currencyService = (CurrencyService) context.getAttribute("currencyService");
        this.objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyDto requestCurrency = new CurrencyDto(name, code, sign);
        CurrencyDto responseCurrency = currencyService.save(requestCurrency);

        String jsonResponse = objectMapper.writeValueAsString(responseCurrency);
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        List<CurrencyDto> currencies = currencyService.getAll();
        String jsonResponse = objectMapper.writeValueAsString(currencies);
        resp.getWriter().write(jsonResponse);
    }
}