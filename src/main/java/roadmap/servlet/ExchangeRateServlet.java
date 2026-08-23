package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.ValidationException;
import roadmap.model.CurrencyCodePair;
import roadmap.model.ExchangeRateResponse;
import roadmap.model.dto.ExchangeRateDto;
import roadmap.service.ExchangeRateService;
import roadmap.validator.CurrencyValidator;
import roadmap.validator.ExchangeRateValidator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
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

        String path = req.getPathInfo();
        String code = path.substring(1);

        String baseCurrencyCode = code.substring(0, 3);
        String targetCurrencyCode = code.substring(3);
        try {
            CurrencyValidator.validateCode(baseCurrencyCode);
            CurrencyValidator.validateCode(targetCurrencyCode);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }
        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);
        ExchangeRateResponse response = exchangeRateService.getByCode(codePair);
        String jsonResponse = objectMapper.writeValueAsString(response);
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        String code = path.substring(1);
        String baseCurrencyCode = code.substring(0, 3);
        String targetCurrencyCode = code.substring(3);

        // doPatch doesn't work correctly with req.getParameter("rate")?
        // temp solution
        String rateString = req.getReader().readLine();
        rateString = rateString.replace("rate=", "");
        Double rate = Double.parseDouble(rateString);
        BigDecimal bigDecimalRate = BigDecimal.valueOf(rate);

        try {
            CurrencyValidator.validateCode(baseCurrencyCode);
            CurrencyValidator.validateCode(targetCurrencyCode);
            ExchangeRateValidator.validateRate(bigDecimalRate);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }

        ExchangeRateDto exchangeRate = new ExchangeRateDto(baseCurrencyCode, targetCurrencyCode, bigDecimalRate);
        ExchangeRateResponse exchangeRateResponse = exchangeRateService.update(exchangeRate);

        String jsonResponse = objectMapper.writeValueAsString(exchangeRateResponse);
        resp.getWriter().write(jsonResponse);
    }
}