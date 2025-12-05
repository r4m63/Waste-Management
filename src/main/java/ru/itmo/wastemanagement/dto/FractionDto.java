package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FractionDto {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private boolean hazardous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

