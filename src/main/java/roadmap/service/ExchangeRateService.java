package roadmap.service;

import roadmap.dao.JdbcExchangeRateDao;
import roadmap.mapper.CurrencyMapper;
import roadmap.mapper.ExchangeRateMapper;
import roadmap.model.dto.request.ExchangeRateRequestDto;
import roadmap.model.dto.request.ExchangeRequestDto;
import roadmap.model.dto.response.ExchangeRateResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.model.dto.response.ExchangeResponseDto;
import roadmap.model.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ExchangeRateService {
    private final static RoundingMode BANK_ROUNDING = RoundingMode.HALF_EVEN;
    private final static int RATE_SCALE = 6;
    private final static int MONEY_DISPLAY_SCALE = 2;
    private final JdbcExchangeRateDao exchangeRateDao;

    public ExchangeRateService(JdbcExchangeRateDao exchangeRateDao) {
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

    public ExchangeResponseDto exchange(ExchangeRequestDto exchangeRequestDto) {
        String baseCurrencyCode = exchangeRequestDto.baseCurrencyCode();
        String targetCurrencyCode = exchangeRequestDto.targetCurrencyCode();
        CurrencyCodePair codePair = new CurrencyCodePair(baseCurrencyCode, targetCurrencyCode);

        ExchangeRateEntity exchangeEntity = findDirect(codePair)
                .or(() -> findReverse(codePair))
                .or(() -> findCross(codePair))
                .orElseThrow(() -> new NoSuchElementException("Exchange rate with code pair %s, %s not found.".formatted(
                        codePair.baseCurrencyCode(), codePair.targetCurrencyCode()
                )));

        BigDecimal amount = exchangeRequestDto.amount();
        BigDecimal convertedAmount = exchangeEntity.rate().multiply(amount);

        return new ExchangeResponseDto(
                CurrencyMapper.INSTANCE.toResponseDto(exchangeEntity.baseCurrencyEntity()),
                CurrencyMapper.INSTANCE.toResponseDto(exchangeEntity.targetCurrencyEntity()),
                exchangeEntity.rate().setScale(MONEY_DISPLAY_SCALE, BANK_ROUNDING),
                amount,
                convertedAmount.setScale(MONEY_DISPLAY_SCALE, BANK_ROUNDING)
        );
    }

    private Optional<ExchangeRateEntity> findDirect(CurrencyCodePair codePair) {
        Optional<ExchangeRateEntity> exchangeRateOpt = exchangeRateDao.findByCodes(codePair);
        if (exchangeRateOpt.isPresent()) {
            ExchangeRateEntity extractedEntity = exchangeRateOpt.get();
            BigDecimal rate = extractedEntity.rate();
            return Optional.of(new ExchangeRateEntity(
                    extractedEntity.id(),
                    extractedEntity.baseCurrencyEntity(),
                    extractedEntity.targetCurrencyEntity(),
                    rate
            ));
        }
        return Optional.empty();
    }

    private Optional<ExchangeRateEntity> findReverse(CurrencyCodePair codePair) {
        CurrencyCodePair reverseCodePair = new CurrencyCodePair(codePair.targetCurrencyCode(), codePair.baseCurrencyCode());
        Optional<ExchangeRateEntity> exchangeRateOpt = exchangeRateDao.findByCodes(reverseCodePair);
        if (exchangeRateOpt.isPresent()) {
            ExchangeRateEntity extractedEntity = exchangeRateOpt.get();
            BigDecimal rate = BigDecimal.ONE.divide(extractedEntity.rate(), RATE_SCALE, BANK_ROUNDING);
            return Optional.of(new ExchangeRateEntity(
                    extractedEntity.id(),
                    extractedEntity.baseCurrencyEntity(),
                    extractedEntity.targetCurrencyEntity(),
                    rate)
            );
        }
        return Optional.empty();
    }

    private Optional<ExchangeRateEntity> findCross(CurrencyCodePair codePair) {
        CurrencyCodePair firstUsdCodePair = new CurrencyCodePair("USD", codePair.baseCurrencyCode());
        CurrencyCodePair secondUsdCodePair = new CurrencyCodePair("USD", codePair.targetCurrencyCode());
        Optional<ExchangeRateEntity> firstExtractedPairOpt = exchangeRateDao.findByCodes(firstUsdCodePair);
        Optional<ExchangeRateEntity> secondExtractedPairOpt = exchangeRateDao.findByCodes(secondUsdCodePair);

        if (firstExtractedPairOpt.isPresent() && secondExtractedPairOpt.isPresent()) {
            ExchangeRateEntity firstExtractedPair = firstExtractedPairOpt.get();
            ExchangeRateEntity secondExtractedPair = secondExtractedPairOpt.get();
            BigDecimal firstRate = firstExtractedPair.rate();
            BigDecimal secondRate = secondExtractedPair.rate();

            BigDecimal rate = firstRate.divide(secondRate, RATE_SCALE, BANK_ROUNDING);

            return Optional.of(new ExchangeRateEntity(
                    null,
                    firstExtractedPair.targetCurrencyEntity(),
                    secondExtractedPair.targetCurrencyEntity(),
                    rate)
            );
        }
        return Optional.empty();
    }
}