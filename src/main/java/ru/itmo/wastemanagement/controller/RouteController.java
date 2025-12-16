package ru.itmo.wastemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.dto.route.RouteAssignDto;
import ru.itmo.wastemanagement.service.RouteService;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public List<RouteDto> getRoutes() {
        return routeService.getAllRoutesWithStops();
    }

    @PostMapping("/auto-generate")
    public ResponseEntity<RouteDto> autoGenerate() {
        RouteDto dto = routeService.autoGenerateFromKioskOrders();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Integer id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<RouteDto> assignDriver(
            @PathVariable Integer id,
            @RequestBody RouteAssignDto dto
    ) {
        RouteDto updated = routeService.assignDriver(id, dto.getDriverId(), dto.getPlannedStartAt(), dto.getPlannedEndAt());
        return ResponseEntity.ok(updated);
    }
}
