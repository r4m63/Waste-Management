package ru.itmo.wastemanagement.dto.containersize;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContainerSizeUpsertDto {

    @NotBlank(message = "Код не может быть пустым")
    @Size(max = 255, message = "Код не должен превышать 255 символов")
    private String code;

    @NotNull(message = "Вместимость должна быть указана")
    @Min(value = 1, message = "Вместимость должна быть больше 0")
    private Integer capacity;

    private Double length;
    private Double width;
    private Double height;

    @Size(max = 1024, message = "Описание не должно превышать 1024 символа")
    private String description;
}
