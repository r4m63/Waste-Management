package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.dto.GarbagePointRowDto;
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
    public GridTableResponse<GarbagePointRowDto> query(@Valid @RequestBody GridTableRequest req) {
        return garbagePointService.queryGrid(req);
    }

    @PostMapping
    public ResponseEntity<?> createGarbagePoint(@Valid @RequestBody GarbagePointDto dto) {
        Integer id = garbagePointService.createNewGarbagePoint(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

}
