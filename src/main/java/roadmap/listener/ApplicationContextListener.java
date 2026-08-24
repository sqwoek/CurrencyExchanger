package roadmap.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import roadmap.dao.CurrencyDao;
import roadmap.dao.ExchangeRateDao;
import roadmap.service.CurrencyService;
import roadmap.service.ExchangeRateService;
import roadmap.util.ServletResponseUtil;
import tools.jackson.databind.ObjectMapper;

@WebListener
public class ApplicationContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao);

        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyDao", currencyDao);
        context.setAttribute("exchangeRateDao", exchangeRateDao);
        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeRateService", exchangeRateService);
        context.setAttribute("objectMapper", objectMapper);

        ServletResponseUtil.init(objectMapper);

        System.out.println("AppContextInitialized");
    }
}