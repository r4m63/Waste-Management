package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.incident.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.incident.IncidentDto;
import ru.itmo.wastemanagement.entity.Incident;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.IncidentRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RouteStopRepository routeStopRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<IncidentDto> getAllIncidents() {
        return incidentRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(IncidentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        return IncidentDto.fromEntity(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> getIncidentsByRouteId(Integer routeId) {
        return incidentRepository.findByRouteId(routeId)
                .stream()
                .map(IncidentDto::fromEntity)
                .toList();
    }

    @Transactional
    public IncidentDto createIncident(IncidentCreateDto dto, String creatorLogin) {
        if (dto.getStopId() == null) {
            throw new BadRequestException("stopId is required");
        }
        if (dto.getType() == null) {
            throw new BadRequestException("type is required");
        }

        RouteStop stop = routeStopRepository.findById(dto.getStopId())
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", dto.getStopId()));

        User creator = null;
        if (creatorLogin != null && !creatorLogin.isBlank()) {
            creator = userRepository.findByLogin(creatorLogin).orElse(null);
        }

        LocalDateTime now = LocalDateTime.now();

        Incident incident = Incident.builder()
                .stop(stop)
                .type(dto.getType())
                .description(dto.getDescription())
                .photoUrl(dto.getPhotoUrl())
                .createdBy(creator)
                .createdAt(now)
                .updatedAt(now)
                .resolved(false)
                .build();

        incident = incidentRepository.save(incident);
        return IncidentDto.fromEntity(incident);
    }

    @Transactional
    public IncidentDto resolveIncident(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        if (incident.isResolved()) {
            throw new BadRequestException("Инцидент уже решён");
        }

        LocalDateTime now = LocalDateTime.now();
        incident.setResolved(true);
        incident.setResolvedAt(now);
        incident.setUpdatedAt(now);

        incident = incidentRepository.save(incident);
        return IncidentDto.fromEntity(incident);
    }
}

