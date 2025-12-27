package ru.itmo.wastemanagement.dto.shift;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.time.LocalDateTime;

@Value
@Builder
public class DriverShiftDto {
    Integer id;
    Integer driverId;
    String driverName;
    String driverLogin;
    Integer vehicleId;
    String vehiclePlate;
    LocalDateTime openedAt;
    LocalDateTime closedAt;
    ShiftStatus status;

    public static DriverShiftDto fromEntity(DriverShift shift) {
        if (shift == null) return null;
        
        var driver = shift.getDriver();
        var vehicle = shift.getVehicle();
        
        return DriverShiftDto.builder()
                .id(shift.getId())
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getName() : null)
                .driverLogin(driver != null ? driver.getLogin() : null)
                .vehicleId(vehicle != null ? vehicle.getId() : null)
                .vehiclePlate(vehicle != null ? vehicle.getPlateNumber() : null)
                .openedAt(shift.getOpenedAt())
                .closedAt(shift.getClosedAt())
                .status(shift.getStatus())
                .build();
    }
}

