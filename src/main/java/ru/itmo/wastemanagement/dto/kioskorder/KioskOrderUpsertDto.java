package ru.itmo.wastemanagement.dto.kioskorder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskOrderUpsertDto {

    @NotNull(message = "Выберите точку сбора (garbagePointId).")
    private Integer garbagePointId;

    @NotNull(message = "Выберите размер контейнера (containerSizeId).")
    private Integer containerSizeId;

    // Кто создал заказ (киоск / оператор) — можно null
    private Integer userId;

    @NotNull(message = "Выберите фракцию (fractionId).")
    private Integer fractionId;

    @DecimalMin(value = "0.0", message = "Вес не может быть отрицательным.")
    private Double weight;

    @NotNull(message = "Укажите статус заказа.")
    private OrderStatus status; // CREATED / CONFIRMED / CANCELLED
}
