package roadmap.model.entity;

import java.math.BigDecimal;

public class ExchangeRateEntity {
    private Long id;
    private final Long baseCurrencyId;
    private final Long targetCurrencyId;
    private final BigDecimal rate;

    public ExchangeRateEntity(Long id, Long baseCurrencyId, Long targetCurrencyId, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public ExchangeRateEntity(Long baseCurrencyId, Long targetCurrencyId, BigDecimal rate) {
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

    public BigDecimal getRate() {
        return rate;
    }
}
