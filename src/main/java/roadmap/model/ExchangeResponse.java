package roadmap.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import roadmap.model.dto.CurrencyDto;

import java.math.BigDecimal;

@JsonPropertyOrder({"baseCurrency", "targetCurrency", "rate", "amount", "convertedAmount"})
public record ExchangeResponse(
        CurrencyDto baseCurrency,
        CurrencyDto targetCurrency,
        Double rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}