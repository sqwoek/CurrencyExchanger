package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.model.CurrencyEntity;
import roadmap.model.dto.CurrencyDto;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public void save(CurrencyDto dto) {
        CurrencyEntity entity = new CurrencyEntity(
                dto.name(),
                dto.code(),
                dto.sign()
        );
        currencyDao.save(entity);
    }

    public CurrencyDto get(String code) {
        CurrencyEntity entity = currencyDao.getByCode(code);
        CurrencyDto dto = new CurrencyDto(
                entity.getName(),
                entity.getCode(),
                entity.getSign()
        );
        return dto;
    }

    public List<CurrencyDto> getAll() {
        List<CurrencyEntity> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(e -> new CurrencyDto(e.getName(), e.getCode(), e.getSign()))
                .collect(Collectors.toList());
    }
}
