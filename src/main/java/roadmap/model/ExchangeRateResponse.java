package roadmap.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import roadmap.model.dto.CurrencyDto;

import java.math.BigDecimal;

@JsonPropertyOrder({"id", "baseCurrency", "targetCurrency", "rate"})
public record ExchangeRateResponse(Long id, CurrencyDto baseCurrency, CurrencyDto targetCurrency, BigDecimal rate) {
}