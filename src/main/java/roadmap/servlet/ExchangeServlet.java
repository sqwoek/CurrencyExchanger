package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.ValidationException;
import roadmap.model.ExchangeResponse;
import roadmap.model.dto.ExchangeDto;
import roadmap.service.ExchangeRateService;
import roadmap.validator.CurrencyValidator;
import roadmap.validator.ExchangeRateValidator;
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

        try {
            CurrencyValidator.validateCode(from);
            CurrencyValidator.validateCode(to);
            ExchangeRateValidator.validateAmount(amount);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }

        ExchangeDto exchangeDto = new ExchangeDto(from, to, amount);
        ExchangeResponse response = exchangeRateService.exchange(exchangeDto);

        String jsonResponse = objectMapper.writeValueAsString(response);
        resp.getWriter().write(jsonResponse);
    }
}