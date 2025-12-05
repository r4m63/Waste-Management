package ru.itmo.wastemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.ContainerSizeDto;
import ru.itmo.wastemanagement.service.ContainerSizeService;

import java.util.List;

/**
 * REST Controller для размеров контейнеров.
 * Публичные эндпоинты для kiosk frontend.
 */
@RestController
@RequestMapping("/api/container-sizes")
@RequiredArgsConstructor
public class ContainerSizeController {

    private final ContainerSizeService containerSizeService;

    /**
     * Получить все размеры контейнеров.
     */
    @GetMapping
    public ResponseEntity<List<ContainerSizeDto>> getAll() {
        List<ContainerSizeDto> sizes = containerSizeService.findAll();
        return ResponseEntity.ok(sizes);
    }

    /**
     * Получить размер контейнера по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContainerSizeDto> getById(@PathVariable Integer id) {
        ContainerSizeDto size = containerSizeService.findById(id);
        return ResponseEntity.ok(size);
    }
}

