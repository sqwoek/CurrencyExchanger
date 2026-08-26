package roadmap.dao;

import roadmap.model.entity.CurrencyEntity;

public interface CurrencyDao extends CrudDao<CurrencyEntity> {
    CurrencyEntity findByCode(String code);
}