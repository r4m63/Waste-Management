package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeRowDto;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeUpsertDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.service.ContainerSizeService;

import java.util.Map;

@RestController
@RequestMapping("/api/container-sizes")
@RequiredArgsConstructor
public class ContainerSizeController {

    private final ContainerSizeService containerSizeService;

    @PostMapping("/query")
    public ResponseEntity<GridTableResponse<ContainerSizeRowDto>> query(@Valid @RequestBody GridTableRequest req) {
        GridTableResponse<ContainerSizeRowDto> response = containerSizeService.queryGrid(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createContainerSize(@Valid @RequestBody ContainerSizeUpsertDto dto) {
        Long id = containerSizeService.createContainerSize(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateContainerSize(
            @PathVariable Long id,
            @Valid @RequestBody ContainerSizeUpsertDto dto
    ) {
        containerSizeService.updateContainerSize(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainerSize(@PathVariable Long id) {
        containerSizeService.deleteContainerSize(id);
        return ResponseEntity.noContent().build();
    }
}
