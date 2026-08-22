package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.ValidationException;
import roadmap.model.dto.ExchangeRateDto;
import roadmap.model.ExchangeRateResponse;
import roadmap.service.ExchangeRateService;
import roadmap.validator.CurrencyValidator;
import roadmap.validator.ExchangeRateValidator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

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
        ExchangeRateDto exchangeRate = new ExchangeRateDto(baseCurrencyCode, targetCurrencyCode, bigDecimalRate);

        ExchangeRateResponse exchangeRateResponse = exchangeRateService.save(exchangeRate);

        String jsonResponse = objectMapper.writeValueAsString(exchangeRateResponse);
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String path = req.getPathInfo();
        ExchangeRateResponse response;
        List<ExchangeRateResponse> exchangeRates;
        String jsonResponse;
        if (path == null || path.equals("/")) {
            exchangeRates = exchangeRateService.getAll();
            jsonResponse = objectMapper.writeValueAsString(exchangeRates);
        } else {
            String code = path.substring(1);

            try {
                CurrencyValidator.validateCode(code);
            } catch (ValidationException ex) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(ex.getMessage());
                return;
            }

            response = exchangeRateService.getByCode(code);
            jsonResponse = objectMapper.writeValueAsString(response);
        }
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
