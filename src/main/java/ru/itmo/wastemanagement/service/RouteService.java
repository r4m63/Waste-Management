package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.RouteCreateDto;
import ru.itmo.wastemanagement.dto.RouteDto;
import ru.itmo.wastemanagement.dto.RouteStopDto;
import ru.itmo.wastemanagement.dto.StopEventDto;
import ru.itmo.wastemanagement.entity.*;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final StopEventRepository stopEventRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverShiftRepository driverShiftRepository;

    @Transactional(readOnly = true)
    public List<RouteDto> findAll() {
        return routeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteDto> findByStatus(RouteStatus status) {
        return routeRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteDto> findByPlannedDate(LocalDateTime date) {
        return routeRepository.findByPlannedDate(date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteDto> findByDriver(Integer driverId) {
        return routeRepository.findByDriverId(driverId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RouteDto> findByDriverAndStatus(Integer driverId, RouteStatus status) {
        return routeRepository.findByDriverIdAndStatus(driverId, status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteDto findById(Integer id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", id));
        return toDto(route);
    }

    @Transactional
    public RouteDto create(RouteCreateDto dto) {
        Route route = new Route();
        route.setPlannedDate(dto.getPlannedDate());
        route.setPlannedStartAt(dto.getPlannedStartAt());
        route.setPlannedEndAt(dto.getPlannedEndAt());
        route.setStatus(RouteStatus.planned);

        if (dto.getDriverId() != null) {
            User driver = userRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getDriverId()));
            route.setDriver(driver);
        }

        if (dto.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId()));
            route.setVehicle(vehicle);
        }

        Route saved = routeRepository.save(route);
        return toDto(saved);
    }

    @Transactional
    public RouteDto update(Integer id, RouteCreateDto dto) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", id));

        route.setPlannedDate(dto.getPlannedDate());
        route.setPlannedStartAt(dto.getPlannedStartAt());
        route.setPlannedEndAt(dto.getPlannedEndAt());

        if (dto.getDriverId() != null) {
            User driver = userRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getDriverId()));
            route.setDriver(driver);
        } else {
            route.setDriver(null);
        }

        if (dto.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId()));
            route.setVehicle(vehicle);
        } else {
            route.setVehicle(null);
        }

        Route saved = routeRepository.save(route);
        return toDto(saved);
    }

    @Transactional
    public RouteDto startRoute(Integer routeId, Integer driverId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getStatus() != RouteStatus.planned) {
            throw new BadRequestException("Route cannot be started. Current status: " + route.getStatus());
        }

        // Проверяем открытую смену водителя
        DriverShift shift = driverShiftRepository.findByDriverIdAndStatus(driverId, ShiftStatus.open)
                .orElseThrow(() -> new BadRequestException("Driver has no open shift"));

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", driverId));

        route.setStatus(RouteStatus.in_progress);
        route.setStartedAt(LocalDateTime.now());
        route.setDriver(driver);
        route.setShift(shift);

        if (route.getVehicle() == null && shift.getVehicle() != null) {
            route.setVehicle(shift.getVehicle());
        }

        Route saved = routeRepository.save(route);
        return toDto(saved);
    }

    @Transactional
    public RouteDto completeRoute(Integer routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getStatus() != RouteStatus.in_progress) {
            throw new BadRequestException("Route cannot be completed. Current status: " + route.getStatus());
        }

        route.setStatus(RouteStatus.completed);
        route.setFinishedAt(LocalDateTime.now());

        Route saved = routeRepository.save(route);
        return toDto(saved);
    }

    @Transactional
    public RouteDto cancelRoute(Integer routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route", "id", routeId));

        if (route.getStatus() == RouteStatus.completed) {
            throw new BadRequestException("Completed routes cannot be cancelled");
        }

        route.setStatus(RouteStatus.cancelled);

        Route saved = routeRepository.save(route);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route", "id", id);
        }
        routeRepository.deleteById(id);
    }

    private RouteDto toDto(Route route) {
        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderBySeqNoAsc(route.getId());

        List<RouteStopDto> stopDtos = stops.stream()
                .map(this::toStopDto)
                .collect(Collectors.toList());

        return RouteDto.builder()
                .id(route.getId())
                .plannedDate(route.getPlannedDate())
                .driverId(route.getDriver() != null ? route.getDriver().getId() : null)
                .driverName(route.getDriver() != null ? route.getDriver().getName() : null)
                .vehicleId(route.getVehicle() != null ? route.getVehicle().getId() : null)
                .vehicleName(route.getVehicle() != null ? route.getVehicle().getName() : null)
                .vehiclePlateNumber(route.getVehicle() != null ? route.getVehicle().getPlateNumber() : null)
                .shiftId(route.getShift() != null ? route.getShift().getId() : null)
                .plannedStartAt(route.getPlannedStartAt())
                .plannedEndAt(route.getPlannedEndAt())
                .startedAt(route.getStartedAt())
                .finishedAt(route.getFinishedAt())
                .status(route.getStatus())
                .stops(stopDtos)
                .build();
    }

    private RouteStopDto toStopDto(RouteStop stop) {
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

