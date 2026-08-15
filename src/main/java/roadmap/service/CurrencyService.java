package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.model.entity.CurrencyEntity;
import roadmap.model.dto.CurrencyDto;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public CurrencyDto save(CurrencyDto dto) {
        CurrencyEntity entity = new CurrencyEntity(
                dto.getName(),
                dto.getCode(),
                dto.getSign()
        );
        CurrencyEntity responseEntity = currencyDao.save(entity);
        return new CurrencyDto(
                responseEntity.getId(),
                responseEntity.getName(),
                responseEntity.getCode(),
                responseEntity.getSign()
        );
    }

    public CurrencyDto get(String code) {
        CurrencyEntity entity = currencyDao.getByCode(code);
        return new CurrencyDto(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.getSign()
        );
    }

    public List<CurrencyDto> getAll() {
        List<CurrencyEntity> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(e -> new CurrencyDto(e.getId(), e.getName(), e.getCode(), e.getSign()))
                .collect(Collectors.toList());
    }
}
