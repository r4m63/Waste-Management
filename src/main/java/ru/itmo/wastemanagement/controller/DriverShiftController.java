package ru.itmo.wastemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;
import ru.itmo.wastemanagement.dto.shift.DriverShiftDto;
import ru.itmo.wastemanagement.dto.shift.ShiftOpenDto;
import ru.itmo.wastemanagement.service.DriverShiftService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class DriverShiftController {

    private final DriverShiftService driverShiftService;

    @GetMapping
    public List<DriverShiftDto> getAllShifts() {
        return driverShiftService.getAllShifts();
    }

    @GetMapping("/driver/{driverId}")
    public List<DriverShiftDto> getShiftsByDriver(@PathVariable Integer driverId) {
        return driverShiftService.getShiftsByDriverId(driverId);
    }

    @GetMapping("/my/current")
    public ResponseEntity<DriverShiftDto> getCurrentShift(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<DriverShiftDto> shift = driverShiftService.getCurrentShiftByDriverLogin(userDetails.getUsername());
        return shift.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/open")
    public ResponseEntity<DriverShiftDto> openShift(
            @RequestBody(required = false) ShiftOpenDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Integer vehicleId = dto != null ? dto.getVehicleId() : null;
        DriverShiftDto shift = driverShiftService.openShift(userDetails.getUsername(), vehicleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(shift);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<DriverShiftDto> closeShift(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        DriverShiftDto shift = driverShiftService.closeShift(id, userDetails.getUsername());
        return ResponseEntity.ok(shift);
    }
}

