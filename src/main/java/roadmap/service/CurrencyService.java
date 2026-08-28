package roadmap.service;

import roadmap.dao.CurrencyDao;
import roadmap.mapper.CurrencyMapper;
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
        CurrencyEntity entity = CurrencyMapper.INSTANCE.toEntity(dto);
        CurrencyEntity savedEntity = currencyDao.save(entity);
        return CurrencyMapper.INSTANCE.toResponseDto(savedEntity);
    }

    public CurrencyResponseDto get(String code) {
        CurrencyEntity entity = currencyDao.findByCode(code);
        return CurrencyMapper.INSTANCE.toResponseDto(entity);
    }

    public List<CurrencyResponseDto> getAll() {
        List<CurrencyEntity> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(CurrencyMapper.INSTANCE::toResponseDto)
                .collect(Collectors.toList());
    }
}
