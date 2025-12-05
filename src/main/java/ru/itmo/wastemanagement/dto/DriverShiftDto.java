package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverShiftDto {
    private Integer id;
    private Integer driverId;
    private String driverName;
    private Integer vehicleId;
    private String vehicleName;
    private String vehiclePlateNumber;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private ShiftStatus status;
}

