package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarbagePointCreateDto {
    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Capacity is required")
    @Min(value = 0, message = "Capacity must be non-negative")
    private Integer capacity;

    @Builder.Default
    private boolean open = true;

    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double lat;

    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double lon;

    private Integer adminId;
    private List<Integer> fractionIds;
}

