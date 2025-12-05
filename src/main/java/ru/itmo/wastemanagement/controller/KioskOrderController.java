package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.KioskOrderCreateDto;
import ru.itmo.wastemanagement.dto.KioskOrderDto;
import ru.itmo.wastemanagement.service.KioskOrderService;

import java.util.List;

/**
 * REST Controller для заказов через киоск.
 * Публичные эндпоинты для kiosk frontend.
 */
@RestController
@RequestMapping("/api/kiosk-orders")
@RequiredArgsConstructor
public class KioskOrderController {

    private final KioskOrderService kioskOrderService;

    /**
     * Получить все заказы.
     */
    @GetMapping
    public ResponseEntity<List<KioskOrderDto>> getAll() {
        List<KioskOrderDto> orders = kioskOrderService.findAll();
        return ResponseEntity.ok(orders);
    }

    /**
     * Получить заказ по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KioskOrderDto> getById(@PathVariable Integer id) {
        KioskOrderDto order = kioskOrderService.findById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Получить заказы по точке сбора.
     */
    @GetMapping("/garbage-point/{garbagePointId}")
    public ResponseEntity<List<KioskOrderDto>> getByGarbagePoint(@PathVariable Integer garbagePointId) {
        List<KioskOrderDto> orders = kioskOrderService.findByGarbagePoint(garbagePointId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Создать новый заказ (для киоска).
     */
    @PostMapping
    public ResponseEntity<KioskOrderDto> create(@Valid @RequestBody KioskOrderCreateDto dto) {
        KioskOrderDto created = kioskOrderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Отменить заказ.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<KioskOrderDto> cancel(@PathVariable Integer id) {
        KioskOrderDto cancelled = kioskOrderService.cancel(id);
        return ResponseEntity.ok(cancelled);
    }
}

