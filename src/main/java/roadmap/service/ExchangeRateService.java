package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.dao.ExchangeRateDao;
import roadmap.model.CurrencyCodePair;
import roadmap.model.ExchangeResponse;
import roadmap.model.dto.ExchangeDto;
import roadmap.model.entity.CurrencyEntity;
import roadmap.model.entity.ExchangeRateEntity;
import roadmap.model.dto.CurrencyDto;
import roadmap.model.dto.ExchangeRateDto;
import roadmap.model.ExchangeRateResponse;
import roadmap.model.entity.ExchangeRateSaveEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    public ExchangeRateResponse save(ExchangeRateDto exchangeRate) {
        ExchangeRateSaveEntity entity = new ExchangeRateSaveEntity(
                exchangeRate.baseCurrencyCode(),
                exchangeRate.targetCurrencyCode(),
                exchangeRate.rate()
        );

        exchangeRateDao.save(entity);

        CurrencyCodePair codePair = new CurrencyCodePair(exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode());
        return getByCode(codePair);
    }

    public ExchangeRateResponse getByCode(CurrencyCodePair codePair) {
        ExchangeRateEntity rateEntity = exchangeRateDao.getByCode(codePair);

        CurrencyEntity baseCurrency = currencyDao.get(rateEntity.getBaseCurrencyId());
        CurrencyEntity targetCurrency = currencyDao.get(rateEntity.getTargetCurrencyId());

        CurrencyDto baseCurrencyDto = new CurrencyDto(baseCurrency.getId(), baseCurrency.getName(), baseCurrency.getCode(), baseCurrency.getSign());
        CurrencyDto targetCurrencyDto = new CurrencyDto(targetCurrency.getId(), targetCurrency.getName(), targetCurrency.getCode(), targetCurrency.getSign());

        return new ExchangeRateResponse(
                rateEntity.getId(),
                baseCurrencyDto,
                targetCurrencyDto,
                rateEntity.getRate()
        );
    }

    public List<ExchangeRateResponse> getAll() {
        List<ExchangeRateEntity> exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateResponse> exchangeRateResponses = new ArrayList<>();

        for (ExchangeRateEntity rateEntity : exchangeRates) {
            CurrencyEntity baseCurrency = currencyDao.get(rateEntity.getBaseCurrencyId());
            CurrencyEntity targetCurrency = currencyDao.get(rateEntity.getTargetCurrencyId());

            if (baseCurrency != null && targetCurrency != null) {
                CurrencyDto baseDto = new CurrencyDto(
                        baseCurrency.getId(),
                        baseCurrency.getCode(),
                        baseCurrency.getName(),
                        baseCurrency.getSign()
                );
                CurrencyDto targetDto = new CurrencyDto(
                        targetCurrency.getId(),
                        targetCurrency.getCode(),
                        targetCurrency.getName(),
                        targetCurrency.getSign()
                );
                exchangeRateResponses.add(
                        new ExchangeRateResponse(
                                rateEntity.getId(),
                                baseDto,
                                targetDto,
                                rateEntity.getRate()
                        ));
            }
        }
        return exchangeRateResponses;
    }

    public ExchangeRateResponse update(ExchangeRateDto exchangeRate) {
        CurrencyCodePair codePair = new CurrencyCodePair(exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode());
        ExchangeRateResponse oldResponse = getByCode(codePair);

        ExchangeRateResponse responseToChange = new ExchangeRateResponse(
                oldResponse.id(),
                oldResponse.baseCurrency(),
                oldResponse.targetCurrency(),
                exchangeRate.rate()
        );

        ExchangeRateEntity updatedEntity = exchangeRateDao.update(responseToChange);
        return new ExchangeRateResponse(
                oldResponse.id(),
                oldResponse.baseCurrency(),
                oldResponse.targetCurrency(),
                updatedEntity.getRate()
        );
    }

    public ExchangeResponse exchange(ExchangeDto exchangeDto) {
        String baseCurrencyCode = exchangeDto.baseCurrencyCode();
        String targetCurrencyCode = exchangeDto.targetCurrencyCode();
        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);
        ExchangeRateResponse entity = getByCode(codePair);
        BigDecimal amount = exchangeDto.amount();

        BigDecimal convertedAmount = entity.rate().multiply(amount);

        return new ExchangeResponse(
                entity.baseCurrency(),
                entity.targetCurrency(),
                entity.rate(),
                amount,
                convertedAmount
        );
    }
}