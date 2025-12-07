package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GarbagePointUpsertDto {

    private Integer id;

    @NotBlank
    private String address;

    @NotNull
    @Min(0)
    private Integer capacity;

    private Boolean open;

    private Double lat;
    private Double lon;

    private Integer kioskId;
}

