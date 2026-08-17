package roadmap.model.dto;

import java.math.BigDecimal;

public record ExchangeDto(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) {
}