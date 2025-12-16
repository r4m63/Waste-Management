package ru.itmo.wastemanagement.dto.route;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RouteAssignDto {

    @NotNull(message = "Укажите водителя")
    private Integer driverId;

    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
}
