package roadmap.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import roadmap.model.dto.request.CurrencyRequestDto;
import roadmap.model.dto.response.CurrencyResponseDto;
import roadmap.model.entity.CurrencyEntity;

@Mapper
public interface CurrencyMapper {
    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

    @Mapping(target = "id", ignore = true)
    CurrencyEntity toEntity(CurrencyRequestDto dto);

    CurrencyResponseDto toResponseDto(CurrencyEntity entity);
}