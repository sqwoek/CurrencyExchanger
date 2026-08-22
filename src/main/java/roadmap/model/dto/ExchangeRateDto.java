package roadmap.model.dto;

import java.math.BigDecimal;

public record ExchangeRateDto(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
}
