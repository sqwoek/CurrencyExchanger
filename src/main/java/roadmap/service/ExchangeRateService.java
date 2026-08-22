package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.dao.ExchangeRateDao;
import roadmap.model.CodePair;
import roadmap.model.ExchangeResponse;
import roadmap.model.dto.ExchangeDto;
import roadmap.model.entity.CurrencyEntity;
import roadmap.model.entity.ExchangeRateEntity;
import roadmap.model.dto.CurrencyDto;
import roadmap.model.dto.ExchangeRateDto;
import roadmap.model.ExchangeRateResponse;

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
        Long baseCurrencyId = currencyDao.getIdByCode(exchangeRate.baseCurrencyCode());
        Long targetCurrencyId = currencyDao.getIdByCode(exchangeRate.targetCurrencyCode());

        if (baseCurrencyId == null || targetCurrencyId == null) {
            throw new RuntimeException("that code isn't saved");
        }

        ExchangeRateEntity entity = new ExchangeRateEntity(
                baseCurrencyId,
                targetCurrencyId,
                exchangeRate.rate()
        );

        ExchangeRateEntity responseEntity = exchangeRateDao.save(entity);

        CurrencyEntity baseCurrency = currencyDao.get(responseEntity.getBaseCurrencyId());
        CurrencyEntity targetCurrency = currencyDao.get(responseEntity.getTargetCurrencyId());

        CurrencyDto baseCurrencyDto = new CurrencyDto(baseCurrency.getId(), baseCurrency.getName(), baseCurrency.getCode(), baseCurrency.getSign());
        CurrencyDto targetCurrencyDto = new CurrencyDto(targetCurrency.getId(), targetCurrency.getName(), targetCurrency.getCode(), targetCurrency.getSign());

        return new ExchangeRateResponse(
                responseEntity.getId(),
                baseCurrencyDto,
                targetCurrencyDto,
                responseEntity.getRate()
        );
    }

    public ExchangeRateResponse getByCode(String code) {
        String baseCurrencyCode = code.substring(0, 3);
        String targetCurrencyCode = code.substring(3);

        Long baseCurrencyId = currencyDao.getIdByCode(baseCurrencyCode);
        Long targetCurrencyId = currencyDao.getIdByCode(targetCurrencyCode);

        CodePair codePair = new CodePair(baseCurrencyId, targetCurrencyId);
        ExchangeRateEntity rateEntity = exchangeRateDao.getByCode(codePair);

        CurrencyEntity baseCurrency = currencyDao.getByCode(baseCurrencyCode);
        CurrencyEntity targetCurrency = currencyDao.getByCode(targetCurrencyCode);

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
        String code = exchangeRate.baseCurrencyCode() + exchangeRate.targetCurrencyCode();
        ExchangeRateResponse oldResponse = getByCode(code);

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
        ExchangeRateResponse entity = getByCode(baseCurrencyCode + targetCurrencyCode);
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