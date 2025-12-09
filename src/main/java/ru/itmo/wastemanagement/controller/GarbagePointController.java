package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointCreateUpdateDto;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointRowDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.service.GarbagePointService;

import java.util.Map;

@RestController
@RequestMapping("/api/garbage-points")
@RequiredArgsConstructor
public class GarbagePointController {

    private final GarbagePointService garbagePointService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody @Valid GridTableRequest req) {
        GridTableResponse<GarbagePointRowDto> res = garbagePointService.queryGrid(req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(res);
    }

    @PostMapping
    public ResponseEntity<?> createGarbagePoint(@RequestBody @Valid GarbagePointCreateUpdateDto dto) {
        Integer id = garbagePointService.createNewGarbagePoint(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody @Valid GarbagePointCreateUpdateDto dto
    ) {
        garbagePointService.update(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        garbagePointService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}