package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.ConnectionManager;
import roadmap.dao.CurrencyDao;
import roadmap.dao.ExchangeRateDao;
import roadmap.model.ExchangeResponse;
import roadmap.model.dto.ExchangeDto;
import roadmap.service.ExchangeRateService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/exchange/*")
public class ExchangeServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
        this.objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String from = req.getParameter("from");
        String to = req.getParameter("to");
        BigDecimal amount = BigDecimal.valueOf(Long.parseLong(req.getParameter("amount")));

        ExchangeDto exchangeDto = new ExchangeDto(from, to, amount);
        ExchangeResponse response = exchangeRateService.exchange(exchangeDto);

        String jsonResponse = objectMapper.writeValueAsString(response);
        resp.getWriter().write(jsonResponse);
    }
}