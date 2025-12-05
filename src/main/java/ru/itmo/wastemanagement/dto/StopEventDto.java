package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.StopEventType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopEventDto {
    private Integer id;
    private Integer stopId;
    private StopEventType eventType;
    private LocalDateTime createdAt;
    private String photoUrl;
    private String comment;
}

