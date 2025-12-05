package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopCreateDto {
    @NotNull(message = "Route ID is required")
    private Integer routeId;

    @Min(value = 1, message = "Sequence number must be at least 1")
    private Integer seqNo; // Если null - будет автоматически назначен

    private Integer garbagePointId; // Либо точка, либо адрес
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address; // Либо адрес, либо точка
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;

    @Min(value = 0, message = "Expected capacity must be non-negative")
    private Integer expectedCapacity;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}

