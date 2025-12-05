package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.FractionCreateDto;
import ru.itmo.wastemanagement.dto.FractionDto;
import ru.itmo.wastemanagement.service.FractionService;

import java.util.List;

/**
 * Admin REST Controller для управления фракциями.
 */
@RestController
@RequestMapping("/api/admin/fractions")
@RequiredArgsConstructor
public class AdminFractionController {

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
     * Создать новую фракцию.
     */
    @PostMapping
    public ResponseEntity<FractionDto> create(@RequestBody FractionCreateDto dto) {
        FractionDto created = fractionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить фракцию.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FractionDto> update(@PathVariable Integer id, @RequestBody FractionCreateDto dto) {
        FractionDto updated = fractionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить фракцию.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        fractionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

