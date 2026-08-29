package roadmap.service;

import roadmap.dao.ExchangeRateDao;
import roadmap.mapper.CurrencyMapper;
import roadmap.model.dto.request.ExchangeRequestDto;
import roadmap.model.dto.response.ExchangeResponseDto;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ExchangeService {
    private final static RoundingMode BANK_ROUNDING = RoundingMode.HALF_EVEN;
    private final static int RATE_SCALE = 6;
    private final static int MONEY_DISPLAY_SCALE = 2;
    private final ExchangeRateDao exchangeRateDao;

    public ExchangeService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
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
                exchangeEntity.rate(),
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