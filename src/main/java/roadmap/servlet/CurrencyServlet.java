package roadmap.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roadmap.ConnectionManager;
import roadmap.dao.CurrencyDao;
import roadmap.model.dto.CurrencyDto;
import roadmap.service.CurrencyService;

@WebServlet("/api/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService;

    public CurrencyServlet() {
        ConnectionManager connectionManager = new ConnectionManager();
        CurrencyDao currencyDao = new CurrencyDao(connectionManager);
        this.currencyService = new CurrencyService(currencyDao);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");
        CurrencyDto currency = new CurrencyDto(name, code, sign);
        currencyService.save(currency);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String path = req.getPathInfo();
        String code = path.substring(1);
        CurrencyDto currency = currencyService.get(code);
        System.out.println(currency);
    }
}