package ru.itmo.wastemanagement.controller.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.RouteStopDto;
import ru.itmo.wastemanagement.dto.StopEventCreateDto;
import ru.itmo.wastemanagement.dto.StopEventDto;
import ru.itmo.wastemanagement.entity.enums.StopStatus;
import ru.itmo.wastemanagement.service.RouteStopService;
import ru.itmo.wastemanagement.service.StopEventService;

import java.util.List;

/**
 * REST Controller для управления остановками маршрута.
 * Эндпоинты для driver-frontend.
 */
@RestController
@RequestMapping("/api/driver/stops")
@RequiredArgsConstructor
public class DriverStopController {

    private final RouteStopService routeStopService;
    private final StopEventService stopEventService;

    /**
     * Получить остановки маршрута.
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<RouteStopDto>> getRouteStops(@PathVariable Integer routeId) {
        List<RouteStopDto> stops = routeStopService.findByRoute(routeId);
        return ResponseEntity.ok(stops);
    }

    /**
     * Получить остановку по ID.
     */
    @GetMapping("/{stopId}")
    public ResponseEntity<RouteStopDto> getStopById(@PathVariable Integer stopId) {
        RouteStopDto stop = routeStopService.findById(stopId);
        return ResponseEntity.ok(stop);
    }

    /**
     * Обновить статус остановки.
     */
    @PutMapping("/{stopId}/status")
    public ResponseEntity<RouteStopDto> updateStatus(
            @PathVariable Integer stopId,
            @RequestParam StopStatus status) {
        RouteStopDto updated = routeStopService.updateStatus(stopId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Обновить фактическую вместимость.
     */
    @PutMapping("/{stopId}/capacity")
    public ResponseEntity<RouteStopDto> updateCapacity(
            @PathVariable Integer stopId,
            @RequestParam Integer actualCapacity) {
        RouteStopDto updated = routeStopService.updateActualCapacity(stopId, actualCapacity);
        return ResponseEntity.ok(updated);
    }

    /**
     * Получить события остановки.
     */
    @GetMapping("/{stopId}/events")
    public ResponseEntity<List<StopEventDto>> getStopEvents(@PathVariable Integer stopId) {
        List<StopEventDto> events = stopEventService.findByStop(stopId);
        return ResponseEntity.ok(events);
    }

    /**
     * Создать событие на остановке.
     */
    @PostMapping("/{stopId}/events")
    public ResponseEntity<StopEventDto> createEvent(
            @PathVariable Integer stopId,
            @RequestBody StopEventCreateDto dto) {
        dto.setStopId(stopId);
        StopEventDto created = stopEventService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

