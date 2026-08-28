package roadmap.service;

import roadmap.dao.ExchangeRateDao;
import roadmap.mapper.ExchangeRateMapper;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.model.entity.ExchangeRateEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

        ExchangeRateEntity savedEntity = exchangeRateDao.saveFromCodes(entity);
        return ExchangeRateMapper.INSTANCE.toResponseDto(savedEntity);
    }

    public ExchangeRateResponseDto getByCode(CurrencyCodePair codePair) {
        Optional<ExchangeRateEntity> rateEntityOpt = exchangeRateDao.findByCodes(codePair);
        if (rateEntityOpt.isPresent()) {
            return ExchangeRateMapper.INSTANCE.toResponseDto(rateEntityOpt.get());
        }
        throw new NoSuchElementException("Exchange rate with code pair %s, %s not found.".formatted(
                codePair.baseCurrencyCode(), codePair.targetCurrencyCode()
        ));
    }

    public List<ExchangeRateResponseDto> getAll() {
        List<ExchangeRateEntity> exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateResponseDto> exchangeRateResponses = new ArrayList<>();

        for (ExchangeRateEntity rateEntity : exchangeRates) {
            exchangeRateResponses.add(ExchangeRateMapper.INSTANCE.toResponseDto(rateEntity));
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
}