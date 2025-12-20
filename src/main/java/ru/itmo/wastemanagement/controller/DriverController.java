package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.driver.DriverCreateUpdateDto;
import ru.itmo.wastemanagement.dto.driver.DriverRowDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.service.DriverService;

import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/query")
    public ResponseEntity<GridTableResponse<DriverRowDto>> query(@Valid @RequestBody GridTableRequest req) {
        GridTableResponse<DriverRowDto> resp = driverService.queryDriverGrid(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createDriver(@Valid @RequestBody DriverCreateUpdateDto dto) {
        Integer id = driverService.createDriver(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDriver(
            @PathVariable Integer id,
            @Valid @RequestBody DriverCreateUpdateDto dto
    ) {
        driverService.updateDriver(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Integer id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
