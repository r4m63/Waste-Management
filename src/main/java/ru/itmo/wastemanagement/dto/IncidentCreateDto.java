package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.IncidentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreateDto {
    @NotNull(message = "Stop ID is required")
    private Integer stopId;

    @NotNull(message = "Incident type is required")
    private IncidentType type;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    private Integer createdById;
}

