package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.StopEventType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopEventCreateDto {
    @NotNull(message = "Stop ID is required")
    private Integer stopId;

    @NotNull(message = "Event type is required")
    private StopEventType eventType;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;
}

