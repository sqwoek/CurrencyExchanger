package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.ExchangeRequestDto;
import roadmap.model.dto.response.ExchangeResponseDto;
import roadmap.service.ExchangeRateService;
import roadmap.util.CurrencyValidatorUtil;
import roadmap.util.ExchangeRateValidatorUtil;
import roadmap.util.ServletResponseUtil;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@WebServlet("/api/exchange/*")
public class ExchangeServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeRateService = (ExchangeRateService) context.getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRequestDto exchangeRequestDto = extractAndValidateExchangeRequest(req);
            ExchangeResponseDto response = exchangeRateService.exchange(exchangeRequestDto);
            ServletResponseUtil.sendSuccessResponse(resp, response);
        } catch (ValidationException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 400, ex.getMessage());
        } catch (NoSuchElementException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 404, ex.getMessage());
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    private static ExchangeRequestDto extractAndValidateExchangeRequest(HttpServletRequest req) {
        String fromCode = req.getParameter("from");
        String toCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        CurrencyValidatorUtil.validateCode(fromCode.toUpperCase());
        CurrencyValidatorUtil.validateCode(toCode.toUpperCase());
        ExchangeRateValidatorUtil.validateAmount(amount);

        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return new ExchangeRequestDto(fromCode.toUpperCase(), toCode.toUpperCase(), bigDecimalAmount);
    }
}