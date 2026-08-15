package roadmap.model.dto;

public record ExchangeRateDto(String baseCurrencyCode, String targetCurrencyCode, Double rate) {
}
