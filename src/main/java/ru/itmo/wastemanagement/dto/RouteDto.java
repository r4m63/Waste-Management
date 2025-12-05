package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    private Integer id;
    private LocalDateTime plannedDate;
    private Integer driverId;
    private String driverName;
    private Integer vehicleId;
    private String vehicleName;
    private String vehiclePlateNumber;
    private Integer shiftId;
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private RouteStatus status;
    private List<RouteStopDto> stops;
}

