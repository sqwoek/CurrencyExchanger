package roadmap.model.entity;

import java.math.BigDecimal;

public record ExchangeRateSaveEntity(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
}