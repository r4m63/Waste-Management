package ru.itmo.wastemanagement.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.GarbagePointCreateDto;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.service.GarbagePointService;

import java.util.List;

/**
 * Admin REST Controller для управления точками сбора.
 */
@RestController
@RequestMapping("/api/admin/garbage-points")
@RequiredArgsConstructor
public class AdminGarbagePointController {

    private final GarbagePointService garbagePointService;

    /**
     * Получить все точки сбора (включая закрытые).
     */
    @GetMapping
    public ResponseEntity<List<GarbagePointDto>> getAll() {
        List<GarbagePointDto> points = garbagePointService.findAll();
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

    /**
     * Создать новую точку сбора.
     */
    @PostMapping
    public ResponseEntity<GarbagePointDto> create(@Valid @RequestBody GarbagePointCreateDto dto) {
        GarbagePointDto created = garbagePointService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить точку сбора.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GarbagePointDto> update(@PathVariable Integer id, @Valid @RequestBody GarbagePointCreateDto dto) {
        GarbagePointDto updated = garbagePointService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить точку сбора.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        garbagePointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

