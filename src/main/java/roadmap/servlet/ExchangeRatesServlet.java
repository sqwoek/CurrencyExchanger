package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.EntityAlreadyExistsException;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.service.ExchangeRateService;
import roadmap.util.ExchangeRateValidatorUtil;
import roadmap.util.ServletResponseUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRateRequestDto exchangeRate = extractAndValidateExchangeRateRequest(req);
            ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.save(exchangeRate);

            ServletResponseUtil.sendSuccessResponse(resp, 201, exchangeRateResponseDto);
        } catch (ValidationException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 400, ex.getMessage());
        } catch (NoSuchElementException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 404, ex.getMessage());
        } catch (EntityAlreadyExistsException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 409, ex.getMessage());
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.getAll();
            ServletResponseUtil.sendSuccessResponse(resp, 200, exchangeRates);
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    private static ExchangeRateRequestDto extractAndValidateExchangeRateRequest(HttpServletRequest req) {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);
        ExchangeRateValidatorUtil.validateCodePair(codePair);
        ExchangeRateValidatorUtil.validateRate(rate);

        BigDecimal bigDecimalRate = new BigDecimal(rate);
        return new ExchangeRateRequestDto(codePair.baseCurrencyCode().toUpperCase(),
                codePair.targetCurrencyCode().toUpperCase(), bigDecimalRate);
    }
}
