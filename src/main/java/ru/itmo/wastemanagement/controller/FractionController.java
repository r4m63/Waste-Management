package ru.itmo.wastemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.FractionDto;
import ru.itmo.wastemanagement.service.FractionService;

import java.util.List;

/**
 * REST Controller для фракций отходов.
 * Публичные эндпоинты для landing и kiosk frontend.
 */
@RestController
@RequestMapping("/api/fractions")
@RequiredArgsConstructor
public class FractionController {

    private final FractionService fractionService;

    /**
     * Получить все фракции.
     */
    @GetMapping
    public ResponseEntity<List<FractionDto>> getAll() {
        List<FractionDto> fractions = fractionService.findAll();
        return ResponseEntity.ok(fractions);
    }

    /**
     * Получить фракцию по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FractionDto> getById(@PathVariable Integer id) {
        FractionDto fraction = fractionService.findById(id);
        return ResponseEntity.ok(fraction);
    }

    /**
     * Получить фракцию по коду.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<FractionDto> getByCode(@PathVariable String code) {
        FractionDto fraction = fractionService.findByCode(code);
        return ResponseEntity.ok(fraction);
    }
}

