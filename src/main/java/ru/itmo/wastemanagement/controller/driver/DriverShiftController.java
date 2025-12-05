package ru.itmo.wastemanagement.controller.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.DriverShiftCreateDto;
import ru.itmo.wastemanagement.dto.DriverShiftDto;
import ru.itmo.wastemanagement.service.DriverShiftService;

import java.util.Optional;

/**
 * REST Controller для управления сменами водителя.
 * Эндпоинты для driver-frontend.
 */
@RestController
@RequestMapping("/api/driver/shifts")
@RequiredArgsConstructor
public class DriverShiftController {

    private final DriverShiftService driverShiftService;

    /**
     * Получить открытую смену водителя.
     */
    @GetMapping("/current/{driverId}")
    public ResponseEntity<DriverShiftDto> getCurrentShift(@PathVariable Integer driverId) {
        Optional<DriverShiftDto> shift = driverShiftService.findOpenShiftByDriver(driverId);
        return shift.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Открыть смену.
     */
    @PostMapping("/open")
    public ResponseEntity<DriverShiftDto> openShift(@RequestBody DriverShiftCreateDto dto) {
        DriverShiftDto opened = driverShiftService.openShift(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(opened);
    }

    /**
     * Закрыть смену водителя.
     */
    @PostMapping("/close/{driverId}")
    public ResponseEntity<DriverShiftDto> closeShift(@PathVariable Integer driverId) {
        DriverShiftDto closed = driverShiftService.closeShift(driverId);
        return ResponseEntity.ok(closed);
    }
}

