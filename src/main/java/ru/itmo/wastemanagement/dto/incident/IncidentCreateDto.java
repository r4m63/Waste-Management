package ru.itmo.wastemanagement.dto.incident;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.itmo.wastemanagement.entity.enums.IncidentType;

@Data
public class IncidentCreateDto {
    
    @NotNull(message = "stopId is required")
    private Integer stopId;
    
    @NotNull(message = "type is required")
    private IncidentType type;
    
    private String description;
    
    private String photoUrl;
}

