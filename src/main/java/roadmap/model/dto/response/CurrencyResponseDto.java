package roadmap.model.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "code", "name", "sign"})
public record CurrencyResponseDto(Long id, String name, String code, String sign) {
}