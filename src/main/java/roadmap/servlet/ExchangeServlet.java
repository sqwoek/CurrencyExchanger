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
import roadmap.model.entity.CurrencyCodePair;
import roadmap.service.ExchangeService;
import roadmap.util.CurrencyValidatorUtil;
import roadmap.util.ExchangeRateValidatorUtil;
import roadmap.util.ServletResponseUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private ExchangeService exchangeService;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.exchangeService = (ExchangeService) context.getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ExchangeRequestDto exchangeRequestDto = extractAndValidateExchangeRequest(req);
            ExchangeResponseDto response = exchangeService.exchange(exchangeRequestDto);
            ServletResponseUtil.sendSuccessResponse(resp, 200, response);
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

        ExchangeRateValidatorUtil.validateCodePair(new CurrencyCodePair(fromCode, toCode));
        ExchangeRateValidatorUtil.validateAmount(amount);

        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return new ExchangeRequestDto(fromCode.toUpperCase(), toCode.toUpperCase(), bigDecimalAmount);
    }
}