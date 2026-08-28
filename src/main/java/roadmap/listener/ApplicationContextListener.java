package roadmap.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import roadmap.dao.CurrencyDao;
import roadmap.dao.ExchangeRateDao;
import roadmap.dao.JdbcCurrencyDao;
import roadmap.dao.JdbcExchangeRateDao;
import roadmap.service.CurrencyService;
import roadmap.service.ExchangeRateService;
import roadmap.service.ExchangeService;
import roadmap.util.ServletResponseUtil;
import tools.jackson.databind.ObjectMapper;

@WebListener
public class ApplicationContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyDao currencyDao = new JdbcCurrencyDao();
        ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
        CurrencyService currencyService = new CurrencyService(currencyDao);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao);
        ExchangeService exchangeService = new ExchangeService(exchangeRateDao);


        ServletContext context = sce.getServletContext();
        context.setAttribute("currencyDao", currencyDao);
        context.setAttribute("exchangeRateDao", exchangeRateDao);
        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeRateService", exchangeRateService);
        context.setAttribute("objectMapper", objectMapper);
        context.setAttribute("exchangeService", exchangeService);

        ServletResponseUtil.init(objectMapper);
    }
}