package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.IncidentDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final RouteStopRepository routeStopRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<IncidentDto> findAll() {
        return incidentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findUnresolved() {
        return incidentRepository.findByResolvedFalseOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentDto> findByStop(Integer stopId) {
        return incidentRepository.findByStopId(stopId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidentDto findById(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        return toDto(incident);
    }

    @Transactional
    public IncidentDto create(IncidentCreateDto dto) {
        RouteStop stop = routeStopRepository.findById(dto.getStopId())
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", dto.getStopId()));

        Incident incident = new Incident();
        incident.setStop(stop);
        incident.setType(dto.getType());
        incident.setDescription(dto.getDescription());
        incident.setPhotoUrl(dto.getPhotoUrl());
        incident.setResolved(false);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        if (dto.getCreatedById() != null) {
            User creator = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));
            incident.setCreatedBy(creator);
        }

        Incident saved = incidentRepository.save(incident);
        return toDto(saved);
    }

    @Transactional
    public IncidentDto update(Integer id, IncidentCreateDto dto) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        incident.setType(dto.getType());
        incident.setDescription(dto.getDescription());
        incident.setPhotoUrl(dto.getPhotoUrl());
        incident.setUpdatedAt(LocalDateTime.now());

        Incident saved = incidentRepository.save(incident);
        return toDto(saved);
    }

    @Transactional
    public IncidentDto resolve(Integer id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        if (incident.isResolved()) {
            throw new BadRequestException("Incident is already resolved");
        }

        incident.setResolved(true);
        incident.setResolvedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        Incident saved = incidentRepository.save(incident);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident", "id", id);
        }
        incidentRepository.deleteById(id);
    }

    private IncidentDto toDto(Incident incident) {
        return IncidentDto.builder()
                .id(incident.getId())
                .stopId(incident.getStop().getId())
                .routeId(incident.getStop().getRoute().getId())
                .type(incident.getType())
                .description(incident.getDescription())
                .photoUrl(incident.getPhotoUrl())
                .createdById(incident.getCreatedBy() != null ? incident.getCreatedBy().getId() : null)
                .createdByName(incident.getCreatedBy() != null ? incident.getCreatedBy().getName() : null)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolved(incident.isResolved())
                .resolvedAt(incident.getResolvedAt())
                .build();
    }
}

