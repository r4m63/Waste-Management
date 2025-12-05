package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.DriverShiftDto;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.service.DriverShiftService;

import java.util.List;

/**
 * Admin REST Controller для управления сменами водителей.
 */
@RestController
@RequestMapping("/api/admin/driver-shifts")
@RequiredArgsConstructor
public class AdminDriverShiftController {

    private final DriverShiftService driverShiftService;

    /**
     * Получить все смены.
     */
    @GetMapping
    public ResponseEntity<List<DriverShiftDto>> getAll() {
        List<DriverShiftDto> shifts = driverShiftService.findAll();
        return ResponseEntity.ok(shifts);
    }

    /**
     * Получить открытые смены.
     */
    @GetMapping("/open")
    public ResponseEntity<List<DriverShiftDto>> getOpen() {
        List<DriverShiftDto> shifts = driverShiftService.findOpenShifts();
        return ResponseEntity.ok(shifts);
    }

    /**
     * Получить смены по статусу.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriverShiftDto>> getByStatus(@PathVariable ShiftStatus status) {
        List<DriverShiftDto> shifts = driverShiftService.findByStatus(status);
        return ResponseEntity.ok(shifts);
    }

    /**
     * Получить смену по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DriverShiftDto> getById(@PathVariable Integer id) {
        DriverShiftDto shift = driverShiftService.findById(id);
        return ResponseEntity.ok(shift);
    }

    /**
     * Закрыть смену по ID.
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<DriverShiftDto> close(@PathVariable Integer id) {
        DriverShiftDto closed = driverShiftService.closeShiftById(id);
        return ResponseEntity.ok(closed);
    }
}

