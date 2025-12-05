package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.KioskOrderDto;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.service.KioskOrderService;

import java.util.List;

/**
 * Admin REST Controller для управления заказами киосков.
 */
@RestController
@RequestMapping("/api/admin/kiosk-orders")
@RequiredArgsConstructor
public class AdminKioskOrderController {

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
     * Получить заказы по статусу.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KioskOrderDto>> getByStatus(@PathVariable OrderStatus status) {
        List<KioskOrderDto> orders = kioskOrderService.findByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Получить заказы по пользователю.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KioskOrderDto>> getByUser(@PathVariable Integer userId) {
        List<KioskOrderDto> orders = kioskOrderService.findByUser(userId);
        return ResponseEntity.ok(orders);
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
     * Получить заказ по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KioskOrderDto> getById(@PathVariable Integer id) {
        KioskOrderDto order = kioskOrderService.findById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Обновить статус заказа.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<KioskOrderDto> updateStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus status) {
        KioskOrderDto updated = kioskOrderService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить заказ.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        kioskOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

