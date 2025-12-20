package ru.itmo.wastemanagement.dto.fraction;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FractionUpsertDto {

    @NotBlank(message = "Название фракции обязательно.")
    private String name;

    @NotBlank(message = "Код фракции обязателен.")
    private String code;

    private String description;

    private boolean hazardous;
}
