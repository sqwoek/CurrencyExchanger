package roadmap.model.dto.request;

import java.math.BigDecimal;

public record ExchangeRequestDto(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) {
}