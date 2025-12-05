package ru.itmo.wastemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.service.GarbagePointService;

import java.util.List;

/**
 * REST Controller для точек сбора мусора.
 * Публичные эндпоинты для landing-frontend.
 */
@RestController
@RequestMapping("/api/garbage-points")
@RequiredArgsConstructor
public class GarbagePointController {

    private final GarbagePointService garbagePointService;

    /**
     * Получить все открытые точки сбора (для карты на landing).
     */
    @GetMapping
    public ResponseEntity<List<GarbagePointDto>> getAllOpen() {
        List<GarbagePointDto> points = garbagePointService.findAllOpen();
        return ResponseEntity.ok(points);
    }

    /**
     * Получить точку по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GarbagePointDto> getById(@PathVariable Integer id) {
        GarbagePointDto point = garbagePointService.findById(id);
        return ResponseEntity.ok(point);
    }
}
