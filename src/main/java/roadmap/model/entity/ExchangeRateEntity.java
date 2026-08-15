package roadmap.model.entity;

public class ExchangeRateEntity {
    private Long id;
    private final Long baseCurrencyId;
    private final Long targetCurrencyId;
    private final Double rate;

    public ExchangeRateEntity(Long id, Long baseCurrencyId, Long targetCurrencyId, Double rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public ExchangeRateEntity(Long baseCurrencyId, Long targetCurrencyId, Double rate) {
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public Long getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public Long getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public Double getRate() {
        return rate;
    }
}
