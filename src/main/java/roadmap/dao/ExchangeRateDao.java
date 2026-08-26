package roadmap.dao;

import roadmap.model.entity.ExchangeRateUpdateEntity;
import roadmap.model.entity.CurrencyCodePair;
import roadmap.model.entity.ExchangeRateEntity;

import java.util.Optional;

public interface ExchangeRateDao extends CrudDao<ExchangeRateEntity> {
    Optional<ExchangeRateEntity> findByCodes(CurrencyCodePair codePair);
    void update(ExchangeRateUpdateEntity exchangeRate);
}