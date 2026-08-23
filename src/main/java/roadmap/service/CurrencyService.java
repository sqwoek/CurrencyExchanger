package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.model.dto.request.CurrencyRequestDto;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.model.entity.CurrencyEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public CurrencyResponseDto save(CurrencyRequestDto dto) {
        CurrencyEntity entity = new CurrencyEntity(
                null,
                dto.name(),
                dto.code(),
                dto.sign()
        );
        CurrencyEntity responseEntity = currencyDao.save(entity);
        return new CurrencyResponseDto(
                responseEntity.id(),
                responseEntity.name(),
                responseEntity.code(),
                responseEntity.sign()
        );
    }

    public CurrencyResponseDto get(String code) {
        CurrencyEntity entity = currencyDao.getByCode(code);
        return new CurrencyResponseDto(
                entity.id(),
                entity.name(),
                entity.code(),
                entity.sign()
        );
    }

    public List<CurrencyResponseDto> getAll() {
        List<CurrencyEntity> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(e -> new CurrencyResponseDto(e.id(), e.name(), e.code(), e.sign()))
                .collect(Collectors.toList());
    }
}
