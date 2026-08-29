package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.EntityAlreadyExistsException;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.CurrencyRequestDto;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.service.CurrencyService;
import roadmap.util.CurrencyValidatorUtil;
import roadmap.util.ServletResponseUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.currencyService = (CurrencyService) context.getAttribute("currencyService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyRequestDto requestCurrency = extractAndValidateDto(req);
            CurrencyResponseDto responseCurrency = currencyService.save(requestCurrency);

            ServletResponseUtil.sendSuccessResponse(resp, 201, responseCurrency);
        } catch (ValidationException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 400, ex.getMessage());
        } catch (EntityAlreadyExistsException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 409, ex.getMessage());
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyResponseDto> currencies = currencyService.getAll();
            ServletResponseUtil.sendSuccessResponse(resp, 200, currencies);
        } catch (DatabaseException ex) {
            ServletResponseUtil.sendErrorResponse(resp, 500, "Internal error.");
        }
    }

    private static CurrencyRequestDto extractAndValidateDto(HttpServletRequest req) {
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyValidatorUtil.validateCode(code);
        CurrencyValidatorUtil.validateName(name);
        CurrencyValidatorUtil.validateSign(sign);

        return new CurrencyRequestDto(name, code.toUpperCase(), sign);
    }
}