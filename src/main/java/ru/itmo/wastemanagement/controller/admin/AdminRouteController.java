package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.RouteCreateDto;
import ru.itmo.wastemanagement.dto.RouteDto;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.service.RouteService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin REST Controller для управления маршрутами.
 */
@RestController
@RequestMapping("/api/admin/routes")
@RequiredArgsConstructor
public class AdminRouteController {

    private final RouteService routeService;

    /**
     * Получить все маршруты.
     */
    @GetMapping
    public ResponseEntity<List<RouteDto>> getAll() {
        List<RouteDto> routes = routeService.findAll();
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить маршруты по статусу.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RouteDto>> getByStatus(@PathVariable RouteStatus status) {
        List<RouteDto> routes = routeService.findByStatus(status);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить маршруты по дате.
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<RouteDto>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<RouteDto> routes = routeService.findByPlannedDate(date);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить маршруты водителя.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RouteDto>> getByDriver(@PathVariable Integer driverId) {
        List<RouteDto> routes = routeService.findByDriver(driverId);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить маршрут по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteDto> getById(@PathVariable Integer id) {
        RouteDto route = routeService.findById(id);
        return ResponseEntity.ok(route);
    }

    /**
     * Создать новый маршрут.
     */
    @PostMapping
    public ResponseEntity<RouteDto> create(@RequestBody RouteCreateDto dto) {
        RouteDto created = routeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить маршрут.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RouteDto> update(@PathVariable Integer id, @RequestBody RouteCreateDto dto) {
        RouteDto updated = routeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Отменить маршрут.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RouteDto> cancel(@PathVariable Integer id) {
        RouteDto cancelled = routeService.cancelRoute(id);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Удалить маршрут.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

