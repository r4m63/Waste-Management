package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.RouteStopCreateDto;
import ru.itmo.wastemanagement.dto.RouteStopDto;
import ru.itmo.wastemanagement.dto.StopEventDto;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.StopEvent;
import ru.itmo.wastemanagement.entity.enums.StopStatus;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.RouteRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.StopEventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final GarbagePointRepository garbagePointRepository;
    private final StopEventRepository stopEventRepository;

    @Transactional(readOnly = true)
    public List<RouteStopDto> findByRoute(Integer routeId) {
        return routeStopRepository.findByRouteIdOrderBySeqNoAsc(routeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteStopDto findById(Integer id) {
        RouteStop stop = routeStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", id));
        return toDto(stop);
    }

    @Transactional
    public RouteStopDto create(RouteStopCreateDto dto) {
        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", dto.getRouteId()));

        // Валидация: либо точка, либо адрес
        if (dto.getGarbagePointId() != null && dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            throw new BadRequestException("Stop must have either garbage point or address, not both");
        }
        if (dto.getGarbagePointId() == null && (dto.getAddress() == null || dto.getAddress().isEmpty())) {
            throw new BadRequestException("Stop must have either garbage point or address");
        }

        RouteStop stop = new RouteStop();
        stop.setRoute(route);
        stop.setStatus(StopStatus.planned);
        stop.setTimeFrom(dto.getTimeFrom());
        stop.setTimeTo(dto.getTimeTo());
        stop.setExpectedCapacity(dto.getExpectedCapacity());
        stop.setNote(dto.getNote());

        if (dto.getGarbagePointId() != null) {
            GarbagePoint point = garbagePointRepository.findById(dto.getGarbagePointId())
                    .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", dto.getGarbagePointId()));
            stop.setGarbagePoint(point);
        } else {
            stop.setAddress(dto.getAddress());
        }

        // Если seqNo не указан, автонумерация будет выполнена триггером в БД
        if (dto.getSeqNo() != null) {
            stop.setSeqNo(dto.getSeqNo());
        } else {
            // Ручная автонумерация на случай если триггер не работает
            Integer maxSeqNo = routeStopRepository.findMaxSeqNoByRouteId(dto.getRouteId());
            stop.setSeqNo(maxSeqNo != null ? maxSeqNo + 1 : 1);
        }

        RouteStop saved = routeStopRepository.save(stop);
        return toDto(saved);
    }

    @Transactional
    public RouteStopDto update(Integer id, RouteStopCreateDto dto) {
        RouteStop stop = routeStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", id));

        stop.setTimeFrom(dto.getTimeFrom());
        stop.setTimeTo(dto.getTimeTo());
        stop.setExpectedCapacity(dto.getExpectedCapacity());
        stop.setNote(dto.getNote());

        if (dto.getSeqNo() != null) {
            stop.setSeqNo(dto.getSeqNo());
        }

        if (dto.getGarbagePointId() != null) {
            GarbagePoint point = garbagePointRepository.findById(dto.getGarbagePointId())
                    .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", dto.getGarbagePointId()));
            stop.setGarbagePoint(point);
            stop.setAddress(null);
        } else if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            stop.setAddress(dto.getAddress());
            stop.setGarbagePoint(null);
        }

        RouteStop saved = routeStopRepository.save(stop);
        return toDto(saved);
    }

    @Transactional
    public RouteStopDto updateStatus(Integer id, StopStatus status) {
        RouteStop stop = routeStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", id));

        stop.setStatus(status);
        RouteStop saved = routeStopRepository.save(stop);
        return toDto(saved);
    }

    @Transactional
    public RouteStopDto updateActualCapacity(Integer id, Integer actualCapacity) {
        RouteStop stop = routeStopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RouteStop", "id", id));

        stop.setActualCapacity(actualCapacity);
        RouteStop saved = routeStopRepository.save(stop);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!routeStopRepository.existsById(id)) {
            throw new ResourceNotFoundException("RouteStop", "id", id);
        }
        routeStopRepository.deleteById(id);
    }

    private RouteStopDto toDto(RouteStop stop) {
        List<StopEvent> events = stopEventRepository.findByStopIdOrderByCreatedAtAsc(stop.getId());

        List<StopEventDto> eventDtos = events.stream()
                .map(event -> StopEventDto.builder()
                        .id(event.getId())
                        .stopId(event.getStop().getId())
                        .eventType(event.getEventType())
                        .createdAt(event.getCreatedAt())
                        .photoUrl(event.getPhotoUrl())
                        .comment(event.getComment())
                        .build())
                .collect(Collectors.toList());

        return RouteStopDto.builder()
                .id(stop.getId())
                .routeId(stop.getRoute().getId())
                .seqNo(stop.getSeqNo())
                .garbagePointId(stop.getGarbagePoint() != null ? stop.getGarbagePoint().getId() : null)
                .garbagePointAddress(stop.getGarbagePoint() != null ? stop.getGarbagePoint().getAddress() : null)
                .garbagePointLat(stop.getGarbagePoint() != null ? stop.getGarbagePoint().getLat() : null)
                .garbagePointLon(stop.getGarbagePoint() != null ? stop.getGarbagePoint().getLon() : null)
                .address(stop.getAddress())
                .timeFrom(stop.getTimeFrom())
                .timeTo(stop.getTimeTo())
                .expectedCapacity(stop.getExpectedCapacity())
                .actualCapacity(stop.getActualCapacity())
                .status(stop.getStatus())
                .note(stop.getNote())
                .events(eventDtos)
                .build();
    }
}

