package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.vehicle.VehicleRowDto;
import ru.itmo.wastemanagement.dto.vehicle.VehicleUpsertDto;
import ru.itmo.wastemanagement.service.VehicleService;

import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@Valid @RequestBody GridTableRequest req) {
        GridTableResponse<VehicleRowDto> resp = vehicleService.queryGrid(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@Valid @RequestBody VehicleUpsertDto dto) {
        Integer id = vehicleService.createVehicle(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleUpsertDto dto
    ) {
        vehicleService.updateVehicle(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
