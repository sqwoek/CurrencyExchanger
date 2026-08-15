package roadmap.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "baseCurrency", "targetCurrency", "rate"})
public record ExchangeRateResponse(Long id, CurrencyDto baseCurrency, CurrencyDto targetCurrency, Double rate) {
}