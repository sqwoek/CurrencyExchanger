package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.EntityAlreadyExists;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.service.ExchangeRateService;
import roadmap.validator.CurrencyValidator;
import roadmap.validator.ExchangeRateValidator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/api/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
        this.objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");

        Double rate = Double.parseDouble((req.getParameter("rate")));
        BigDecimal bigDecimalRate = BigDecimal.valueOf(rate);
        try {
            CurrencyValidator.validateCode(baseCurrencyCode);
            CurrencyValidator.validateCode(targetCurrencyCode);
            //TODO: method for parsing String rate -> BigDecimalRate
            ExchangeRateValidator.validateRate(bigDecimalRate);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }
        ExchangeRateRequestDto exchangeRate = new ExchangeRateRequestDto(baseCurrencyCode, targetCurrencyCode, bigDecimalRate);

        try {
            ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.save(exchangeRate);
            String jsonResponse = objectMapper.writeValueAsString(exchangeRateResponseDto);
            resp.getWriter().write(jsonResponse);
        } catch (NoSuchElementException ex) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(ex.getMessage());
        } catch (EntityAlreadyExists ex) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(ex.getMessage());
        } catch (DatabaseException ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(ex.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            try {
                List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.getAll();
                String jsonResponse = objectMapper.writeValueAsString(exchangeRates);
                resp.getWriter().write(jsonResponse);
            } catch (DatabaseException ex) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(ex.getMessage());
            }

        }
    }
}
