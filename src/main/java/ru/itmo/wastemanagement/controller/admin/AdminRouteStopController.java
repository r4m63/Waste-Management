package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.RouteStopCreateDto;
import ru.itmo.wastemanagement.dto.RouteStopDto;
import ru.itmo.wastemanagement.service.RouteStopService;

import java.util.List;

/**
 * Admin REST Controller для управления остановками маршрутов.
 */
@RestController
@RequestMapping("/api/admin/route-stops")
@RequiredArgsConstructor
public class AdminRouteStopController {

    private final RouteStopService routeStopService;

    /**
     * Получить остановки маршрута.
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<RouteStopDto>> getByRoute(@PathVariable Integer routeId) {
        List<RouteStopDto> stops = routeStopService.findByRoute(routeId);
        return ResponseEntity.ok(stops);
    }

    /**
     * Получить остановку по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteStopDto> getById(@PathVariable Integer id) {
        RouteStopDto stop = routeStopService.findById(id);
        return ResponseEntity.ok(stop);
    }

    /**
     * Создать новую остановку.
     */
    @PostMapping
    public ResponseEntity<RouteStopDto> create(@RequestBody RouteStopCreateDto dto) {
        RouteStopDto created = routeStopService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить остановку.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RouteStopDto> update(@PathVariable Integer id, @RequestBody RouteStopCreateDto dto) {
        RouteStopDto updated = routeStopService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить остановку.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routeStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

