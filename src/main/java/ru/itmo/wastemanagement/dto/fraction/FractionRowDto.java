package ru.itmo.wastemanagement.dto.fraction;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.Fraction;

@Value
@Builder
public class FractionRowDto {

    Integer id;
    String name;
    String code;
    String description;
    boolean hazardous;

    public static FractionRowDto fromEntity(Fraction fraction) {
        return FractionRowDto.builder()
                .id(fraction.getId())
                .name(fraction.getName())
                .code(fraction.getCode())
                .description(fraction.getDescription())
                .hazardous(fraction.isHazardous())
                .build();
    }
}
