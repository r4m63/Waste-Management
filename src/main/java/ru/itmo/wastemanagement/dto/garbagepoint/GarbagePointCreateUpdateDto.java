package ru.itmo.wastemanagement.dto.garbagepoint;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.GarbagePoint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarbagePointCreateUpdateDto {

    @NotNull(message = "Адрес обязателен и не может быть пустым")
    @NotBlank(message = "Адрес обязателен и не может быть пустым")
    private String address;
    @NotNull(message = "Вместимость обязательна")
    @Positive(message = "Вместимость должна быть положительным числом")
    @Max(value = 100_000, message = "Вместимость не может превышать {value}")
    private Integer capacity;
    @NotNull(message = "Поле \"open\" обязательно")
    private Boolean open;
    @NotNull(message = "Широта обязательна")
    @DecimalMin(value = "-90.0", message = "Широта не может быть меньше -90")
    @DecimalMax(value = "90.0", message = "Широта не может быть больше 90")
    private Double lat;
    @NotNull(message = "Долгота обязательна")
    @DecimalMin(value = "-180.0", message = "Долгота не может быть меньше -180")
    @DecimalMax(value = "180.0", message = "Долгота не может быть больше 180")
    private Double lon;
    @NotNull(message = "Привязка киоска обязательна")
    @Positive(message = "ID киоска должен быть положительным числом")
    private Integer kioskId;

    public static GarbagePointCreateUpdateDto toDto(GarbagePoint gp) {
        if (gp == null) return null;
        return GarbagePointCreateUpdateDto.builder()
                .address(gp.getAddress())
                .capacity(gp.getCapacity())
                .open(gp.isOpen())
                .lat(gp.getLat())
                .lon(gp.getLon())
                .kioskId(gp.getKiosk() != null ? gp.getKiosk().getId() : null)
                .build();
    }

    public static GarbagePoint toEntity(GarbagePointCreateUpdateDto dto, GarbagePoint targetOrNull) {
        GarbagePoint t = targetOrNull != null ? targetOrNull : new GarbagePoint();
        t.setAddress(dto.getAddress());
        t.setCapacity(dto.getCapacity());
        t.setOpen(dto.getOpen() != null ? dto.getOpen() : true);
        t.setLat(dto.getLat());
        t.setLon(dto.getLon());
        return t;
    }

}
