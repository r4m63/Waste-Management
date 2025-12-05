package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.VehicleCreateDto;
import ru.itmo.wastemanagement.dto.VehicleDto;
import ru.itmo.wastemanagement.service.VehicleService;

import java.util.List;

/**
 * Admin REST Controller для управления транспортом.
 */
@RestController
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleService vehicleService;

    /**
     * Получить все транспортные средства.
     */
    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAll() {
        List<VehicleDto> vehicles = vehicleService.findAll();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Получить активные транспортные средства.
     */
    @GetMapping("/active")
    public ResponseEntity<List<VehicleDto>> getActive() {
        List<VehicleDto> vehicles = vehicleService.findAllActive();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Получить транспорт по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getById(@PathVariable Integer id) {
        VehicleDto vehicle = vehicleService.findById(id);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Создать новый транспорт.
     */
    @PostMapping
    public ResponseEntity<VehicleDto> create(@RequestBody VehicleCreateDto dto) {
        VehicleDto created = vehicleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить транспорт.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> update(@PathVariable Integer id, @RequestBody VehicleCreateDto dto) {
        VehicleDto updated = vehicleService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить транспорт.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

