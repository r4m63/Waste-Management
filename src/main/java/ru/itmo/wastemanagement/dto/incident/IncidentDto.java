package ru.itmo.wastemanagement.dto.incident;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.Incident;
import ru.itmo.wastemanagement.entity.enums.IncidentType;

import java.time.LocalDateTime;

@Value
@Builder
public class IncidentDto {
    Integer id;
    Integer stopId;
    String stopAddress;
    Integer routeId;
    IncidentType type;
    String description;
    String photoUrl;
    Integer createdById;
    String createdByName;
    String createdByLogin;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    boolean resolved;
    LocalDateTime resolvedAt;

    public static IncidentDto fromEntity(Incident incident) {
        if (incident == null) return null;
        
        var stop = incident.getStop();
        var createdBy = incident.getCreatedBy();
        
        return IncidentDto.builder()
                .id(incident.getId())
                .stopId(stop != null ? stop.getId() : null)
                .stopAddress(stop != null ? stop.getAddress() : null)
                .routeId(stop != null && stop.getRoute() != null ? stop.getRoute().getId() : null)
                .type(incident.getType())
                .description(incident.getDescription())
                .photoUrl(incident.getPhotoUrl())
                .createdById(createdBy != null ? createdBy.getId() : null)
                .createdByName(createdBy != null ? createdBy.getName() : null)
                .createdByLogin(createdBy != null ? createdBy.getLogin() : null)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolved(incident.isResolved())
                .resolvedAt(incident.getResolvedAt())
                .build();
    }
}

