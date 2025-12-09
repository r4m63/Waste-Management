package ru.itmo.wastemanagement.dto.vehicle;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.Vehicle;

import java.time.LocalDateTime;

@Value
@Builder
public class VehicleRowDto {

    Integer id;
    String plateNumber;
    String name;
    Integer capacity;
    Boolean active;
    LocalDateTime createdAt;

    public static VehicleRowDto fromEntity(Vehicle v) {
        if (v == null) return null;
        return VehicleRowDto.builder()
                .id(v.getId())
                .plateNumber(v.getPlateNumber())
                .name(v.getName())
                .capacity(v.getCapacity())
                .active(v.isActive())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
