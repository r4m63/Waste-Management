package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreateDto {
    @NotBlank(message = "Plate number is required")
    @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$", 
            message = "Invalid Russian plate number format")
    private String plateNumber;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Builder.Default
    private boolean active = true;
}

