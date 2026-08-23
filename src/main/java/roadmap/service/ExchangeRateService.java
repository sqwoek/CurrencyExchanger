package roadmap.service;

import roadmap.dao.ExchangeRateDao;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.request.ExchangeRequestDto;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.model.dto.response.ExchangeResponseDto;
import roadmap.model.entity.CurrencyEntity;
import roadmap.model.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeRateResponseDto save(ExchangeRateRequestDto exchangeRate) {
        ExchangeRateUpdateEntity entity = new ExchangeRateUpdateEntity(
                exchangeRate.baseCurrencyCode(),
                exchangeRate.targetCurrencyCode(),
                exchangeRate.rate()
        );

        exchangeRateDao.save(entity);

        CurrencyCodePair codePair = new CurrencyCodePair(exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode());
        return getByCode(codePair);
    }

    public ExchangeRateResponseDto getByCode(CurrencyCodePair codePair) {
        ExchangeRateEntity rateEntity = exchangeRateDao.findByCodes(codePair);

        CurrencyEntity baseCurrencyEntity = rateEntity.baseCurrencyEntity();
        CurrencyEntity targetCurrencyEntity = rateEntity.targetCurrencyEntity();

        CurrencyResponseDto baseCurrencyResponseDto = new CurrencyResponseDto(baseCurrencyEntity.id(), baseCurrencyEntity.name(),
                baseCurrencyEntity.code(), baseCurrencyEntity.sign());
        CurrencyResponseDto targetCurrencyResponseDto = new CurrencyResponseDto(targetCurrencyEntity.id(), targetCurrencyEntity.name(),
                targetCurrencyEntity.code(), targetCurrencyEntity.sign());

        return new ExchangeRateResponseDto(
                rateEntity.id(),
                baseCurrencyResponseDto,
                targetCurrencyResponseDto,
                rateEntity.rate()
        );
    }

    public List<ExchangeRateResponseDto> getAll() {
        List<ExchangeRateEntity> exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateResponseDto> exchangeRateResponses = new ArrayList<>();

        for (ExchangeRateEntity rateEntity : exchangeRates) {
            CurrencyEntity baseCurrencyEntity = rateEntity.baseCurrencyEntity();
            CurrencyEntity targetCurrencyEntity = rateEntity.targetCurrencyEntity();

            CurrencyResponseDto baseDto = new CurrencyResponseDto(
                    baseCurrencyEntity.id(),
                    baseCurrencyEntity.name(),
                    baseCurrencyEntity.code(),
                    baseCurrencyEntity.sign()
            );
            CurrencyResponseDto targetDto = new CurrencyResponseDto(
                    targetCurrencyEntity.id(),
                    targetCurrencyEntity.name(),
                    targetCurrencyEntity.code(),
                    targetCurrencyEntity.sign()
            );
            exchangeRateResponses.add(
                    new ExchangeRateResponseDto(
                            rateEntity.id(),
                            baseDto,
                            targetDto,
                            rateEntity.rate()
                    ));
            }
        return exchangeRateResponses;
    }

    public ExchangeRateResponseDto update(ExchangeRateRequestDto exchangeRate) {
        CurrencyCodePair codePair = new CurrencyCodePair(exchangeRate.baseCurrencyCode(), exchangeRate.targetCurrencyCode());
        ExchangeRateUpdateEntity entity = new ExchangeRateUpdateEntity(
                exchangeRate.baseCurrencyCode(),
                exchangeRate.targetCurrencyCode(),
                exchangeRate.rate()
        );
        exchangeRateDao.update(entity);
        return getByCode(codePair);
    }

    public ExchangeResponseDto exchange(ExchangeRequestDto exchangeRequestDto) {
        String baseCurrencyCode = exchangeRequestDto.baseCurrencyCode();
        String targetCurrencyCode = exchangeRequestDto.targetCurrencyCode();
        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);
        ExchangeRateResponseDto entity = getByCode(codePair);
        BigDecimal amount = exchangeRequestDto.amount();


        BigDecimal convertedAmount = entity.rate().multiply(amount);

        return new ExchangeResponseDto(
                entity.baseCurrency(),
                entity.targetCurrency(),
                entity.rate(),
                amount,
                convertedAmount
        );
    }
}