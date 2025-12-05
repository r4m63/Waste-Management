package ru.itmo.wastemanagement.controller.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.RouteDto;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.service.RouteService;

import java.util.List;

/**
 * REST Controller для маршрутов водителя.
 * Эндпоинты для driver-frontend.
 */
@RestController
@RequestMapping("/api/driver/routes")
@RequiredArgsConstructor
public class DriverRouteController {

    private final RouteService routeService;

    /**
     * Получить маршруты водителя.
     */
    @GetMapping("/{driverId}")
    public ResponseEntity<List<RouteDto>> getDriverRoutes(@PathVariable Integer driverId) {
        List<RouteDto> routes = routeService.findByDriver(driverId);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить активный маршрут водителя (в процессе).
     */
    @GetMapping("/{driverId}/active")
    public ResponseEntity<List<RouteDto>> getActiveRoutes(@PathVariable Integer driverId) {
        List<RouteDto> routes = routeService.findByDriverAndStatus(driverId, RouteStatus.in_progress);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить запланированные маршруты водителя.
     */
    @GetMapping("/{driverId}/planned")
    public ResponseEntity<List<RouteDto>> getPlannedRoutes(@PathVariable Integer driverId) {
        List<RouteDto> routes = routeService.findByDriverAndStatus(driverId, RouteStatus.planned);
        return ResponseEntity.ok(routes);
    }

    /**
     * Получить маршрут по ID.
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<RouteDto> getRouteById(@PathVariable Integer routeId) {
        RouteDto route = routeService.findById(routeId);
        return ResponseEntity.ok(route);
    }

    /**
     * Начать маршрут.
     */
    @PostMapping("/route/{routeId}/start")
    public ResponseEntity<RouteDto> startRoute(
            @PathVariable Integer routeId,
            @RequestParam Integer driverId) {
        RouteDto started = routeService.startRoute(routeId, driverId);
        return ResponseEntity.ok(started);
    }

    /**
     * Завершить маршрут.
     */
    @PostMapping("/route/{routeId}/complete")
    public ResponseEntity<RouteDto> completeRoute(@PathVariable Integer routeId) {
        RouteDto completed = routeService.completeRoute(routeId);
        return ResponseEntity.ok(completed);
    }
}

