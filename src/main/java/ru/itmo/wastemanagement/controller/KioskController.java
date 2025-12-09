package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateUpdateDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskRowDto;
import ru.itmo.wastemanagement.service.KioskService;

import java.util.Map;

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class KioskController {

    private final KioskService kioskService;

    @PostMapping
    public ResponseEntity<?> createKioskUser(@RequestBody @Valid KioskCreateUpdateDto dto) {
        Integer id = kioskService.createKioskUser(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody @Valid GridTableRequest req) {
        GridTableResponse<KioskRowDto> body = kioskService.queryKioskGrid(req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKioskUser(
            @PathVariable Integer id,
            @RequestBody @Valid KioskCreateUpdateDto dto
    ) {
        kioskService.updateKioskUser(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKioskUser(@PathVariable Integer id) {
        kioskService.deleteKioskUser(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }


}
