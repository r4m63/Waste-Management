package ru.itmo.wastemanagement.dto.route;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

@Getter
@Setter
public class RouteStopUpdateDto {

    @NotNull(message = "Укажите статус остановки")
    private StopStatus status;

    private Integer actualCapacity;

    private String note;
}

