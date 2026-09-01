package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.service.ExchangeRateService;
import roadmap.util.ExchangeRateValidatorUtil;
import roadmap.util.ServletResponseUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyCodePair codePair = extractAndValidateCodePair(req);
            ExchangeRateResponseDto response = exchangeRateService.getByCode(codePair);

            ServletResponseUtil.sendSuccessResponse(resp, 200, response);
        } catch (ValidationException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 400, ex.getMessage());
        } catch (NoSuchElementException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 404, ex.getMessage());
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRateRequestDto exchangeRate = extractAndValidateExchangeRateRequest(req);
            ExchangeRateResponseDto exchangeRateResponseDto = exchangeRateService.update(exchangeRate);

            ServletResponseUtil.sendSuccessResponse(resp, 200, exchangeRateResponseDto);
        } catch (ValidationException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 400, ex.getMessage());
        } catch (NoSuchElementException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 404, ex.getMessage());
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    private ExchangeRateRequestDto extractAndValidateExchangeRateRequest(HttpServletRequest req) throws IOException {
        CurrencyCodePair codePair = extractAndValidateCodePair(req);
        String rateString = req.getReader().readLine();

        if (rateString == null) {
            throw new ValidationException(ExchangeRateValidatorUtil.MISSING_RATE_MESSAGE);
        }

        rateString = rateString.replace("rate=", "");

        ExchangeRateValidatorUtil.validateRate(rateString);
        BigDecimal bigDecimalRate = new BigDecimal(rateString);
        return new ExchangeRateRequestDto(codePair.baseCurrencyCode(), codePair.targetCurrencyCode(), bigDecimalRate);
    }

    private static CurrencyCodePair extractAndValidateCodePair(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            throw new ValidationException(ExchangeRateValidatorUtil.MISSING_CODE_PAIR_MESSAGE);
        }
        String inputCodePair = path.substring(1);
        if (inputCodePair.length() != 6) {
            throw new ValidationException(ExchangeRateValidatorUtil.MISSING_CODE_PAIR_MESSAGE);
        }
        String baseCurrencyCode = inputCodePair.substring(0, 3);
        String targetCurrencyCode = inputCodePair.substring(3);

        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);
        ExchangeRateValidatorUtil.validateCodePair(codePair);
        return codePair;
    }
}