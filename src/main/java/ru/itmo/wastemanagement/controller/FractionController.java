package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.fraction.FractionRowDto;
import ru.itmo.wastemanagement.dto.fraction.FractionUpsertDto;
import ru.itmo.wastemanagement.service.FractionService;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/fractions")
@RequiredArgsConstructor
public class FractionController {

    private final FractionService fractionService;

    @PostMapping("/query")
    public ResponseEntity<GridTableResponse<FractionRowDto>> query(@Valid @RequestBody GridTableRequest req) {
        GridTableResponse<FractionRowDto> response = fractionService.queryFractionGrid(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createFraction(@Valid @RequestBody FractionUpsertDto dto) {
        Integer id = fractionService.createFraction(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFraction(
            @PathVariable Integer id,
            @Valid @RequestBody FractionUpsertDto dto
    ) {
        fractionService.updateFraction(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFraction(@PathVariable Integer id) {
        fractionService.deleteFraction(id);
        return ResponseEntity.noContent().build();
    }
}
