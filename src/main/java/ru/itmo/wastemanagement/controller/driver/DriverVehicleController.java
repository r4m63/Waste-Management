package ru.itmo.wastemanagement.controller.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.VehicleDto;
import ru.itmo.wastemanagement.service.VehicleService;

import java.util.List;

/**
 * REST Controller для получения списка транспорта.
 * Эндпоинты для driver-frontend.
 */
@RestController
@RequestMapping("/api/driver/vehicles")
@RequiredArgsConstructor
public class DriverVehicleController {

    private final VehicleService vehicleService;

    /**
     * Получить активные транспортные средства.
     */
    @GetMapping
    public ResponseEntity<List<VehicleDto>> getActiveVehicles() {
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
}

