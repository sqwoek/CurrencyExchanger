package roadmap.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.exception.DatabaseException;
import roadmap.exception.EntityAlreadyExists;
import roadmap.exception.ValidationException;
import roadmap.model.dto.request.CurrencyRequestDto;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.service.CurrencyService;
import roadmap.validator.CurrencyValidator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/currencies/*")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService currencyService;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        this.currencyService = (CurrencyService) context.getAttribute("currencyService");
        this.objectMapper = (ObjectMapper) context.getAttribute("objectMapper");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyRequestDto requestCurrency = new CurrencyRequestDto(name, code, sign);

        try {
            CurrencyValidator.validate(requestCurrency);
        } catch (ValidationException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(ex.getMessage());
            return;
        }

        try {
            CurrencyResponseDto responseCurrency = currencyService.save(requestCurrency);
            String jsonResponse = objectMapper.writeValueAsString(responseCurrency);
            resp.getWriter().write(jsonResponse);
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

        List<CurrencyResponseDto> currencies = currencyService.getAll();
        String jsonResponse = objectMapper.writeValueAsString(currencies);
        resp.getWriter().write(jsonResponse);
    }
}