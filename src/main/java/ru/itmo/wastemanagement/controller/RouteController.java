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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.dto.route.RouteAssignDto;
import ru.itmo.wastemanagement.dto.route.RouteStopUpdateDto;
import ru.itmo.wastemanagement.service.RouteService;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;

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

    @GetMapping("/my")
    public List<RouteDto> getMyRoutes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return List.of();
        }
        return routeService.getRoutesForDriverLogin(userDetails.getUsername());
    }

    @GetMapping("/{id}/my")
    public ResponseEntity<RouteDto> getMyRoute(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RouteDto dto = routeService.getMyRoute(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
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

    @PutMapping("/{id}/accept")
    public ResponseEntity<RouteDto> acceptRoute(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // backward-compatible alias for /start
        RouteDto updated = routeService.startRoute(id, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<RouteDto> startRoute(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RouteDto updated = routeService.startRoute(id, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{routeId}/stops/{stopId}")
    public ResponseEntity<RouteDto> updateStop(
            @PathVariable Integer routeId,
            @PathVariable Integer stopId,
            @Valid @RequestBody RouteStopUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RouteDto updated = routeService.updateStop(routeId, stopId, userDetails.getUsername(), dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<RouteDto> finishRoute(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RouteDto updated = routeService.finishRoute(id, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }
}
