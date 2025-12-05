package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverShiftCreateDto {
    @NotNull(message = "Driver ID is required")
    private Integer driverId;

    private Integer vehicleId;
}

