package ru.itmo.wastemanagement.dto.route;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class RouteDto {
    Integer id;
    LocalDate plannedDate;
    RouteStatus status;
    LocalDateTime plannedStartAt;
    LocalDateTime plannedEndAt;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    Integer driverId;
    Integer vehicleId;
    Integer shiftId;
    List<RouteStopDto> stops;
}
