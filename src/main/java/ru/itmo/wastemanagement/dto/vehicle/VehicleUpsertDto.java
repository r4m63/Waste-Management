package ru.itmo.wastemanagement.dto.vehicle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleUpsertDto {

    @NotBlank(message = "Госномер не может быть пустым")
    @Size(max = 255, message = "Госномер не должен превышать 255 символов")
    private String plateNumber;

    @Size(max = 255, message = "Имя не должно превышать 255 символов")
    private String name;

    @NotNull(message = "Вместимость должна быть указана")
    @Min(value = 0, message = "Вместимость не может быть отрицательной")
    private Integer capacity;

    @NotNull(message = "Флаг активности должен быть указан")
    private Boolean active;
}
