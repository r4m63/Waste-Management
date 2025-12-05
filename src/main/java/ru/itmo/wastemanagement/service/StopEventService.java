package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.StopEventCreateDto;
import ru.itmo.wastemanagement.dto.StopEventDto;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.StopEvent;
import ru.itmo.wastemanagement.entity.enums.StopEventType;
import ru.itmo.wastemanagement.entity.enums.StopStatus;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.StopEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StopEventService {

    private final StopEventRepository stopEventRepository;
    private final RouteStopRepository routeStopRepository;

    @Transactional(readOnly = true)
    public List<StopEventDto> findByStop(Integer stopId) {
        return stopEventRepository.findByStopIdOrderByCreatedAtAsc(stopId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StopEventDto findById(Integer id) {
        StopEvent event = stopEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StopEvent", "id", id));
        return toDto(event);
    }

    @Transactional
    public StopEventDto create(StopEventCreateDto dto) {
        RouteStop stop = routeStopRepository.findById(dto.getStopId())
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", dto.getStopId()));

        StopEvent event = new StopEvent();
        event.setStop(stop);
        event.setEventType(dto.getEventType());
        event.setPhotoUrl(dto.getPhotoUrl());
        event.setComment(dto.getComment());
        event.setCreatedAt(LocalDateTime.now());

        // Автоматически обновляем статус остановки
        updateStopStatus(stop, dto.getEventType());

        StopEvent saved = stopEventRepository.save(event);
        return toDto(saved);
    }

    private void updateStopStatus(RouteStop stop, StopEventType eventType) {
        StopStatus newStatus = switch (eventType) {
            case start -> StopStatus.enroute;
            case arrived -> StopStatus.arrived;
            case loading -> StopStatus.loading;
            case unloading -> StopStatus.unloading;
            case done -> StopStatus.done;
            case skipped -> StopStatus.skipped;
            case unavailable -> StopStatus.unavailable;
            case comment -> stop.getStatus(); // Не меняем статус
        };

        stop.setStatus(newStatus);
        routeStopRepository.save(stop);
    }

    @Transactional
    public void delete(Integer id) {
        if (!stopEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("StopEvent", "id", id);
        }
        stopEventRepository.deleteById(id);
    }

    private StopEventDto toDto(StopEvent event) {
        return StopEventDto.builder()
                .id(event.getId())
                .stopId(event.getStop().getId())
                .eventType(event.getEventType())
                .createdAt(event.getCreatedAt())
                .photoUrl(event.getPhotoUrl())
                .comment(event.getComment())
                .build();
    }
}

