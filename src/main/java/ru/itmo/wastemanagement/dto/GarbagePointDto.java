package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarbagePointDto {
    private Integer id;
    private String address;
    private Integer capacity;
    private boolean open;
    private Double lat;
    private Double lon;
    private LocalDateTime createdAt;
    private Integer adminId;
    private String adminName;
    private List<FractionDto> fractions;
}

