package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCreateDto {
    @NotNull(message = "Planned date is required")
    private LocalDateTime plannedDate;

    private Integer driverId;
    private Integer vehicleId;
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
}

