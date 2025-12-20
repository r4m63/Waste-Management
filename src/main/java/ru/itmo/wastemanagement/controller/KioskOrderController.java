package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderRowDto;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderUpsertDto;
import ru.itmo.wastemanagement.service.KioskOrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/kiosk-orders")
@RequiredArgsConstructor
public class KioskOrderController {

    private final KioskOrderService kioskOrderService;

    @PostMapping("/query")
    public ResponseEntity<GridTableResponse<KioskOrderRowDto>> query(@Valid @RequestBody GridTableRequest req) {
        GridTableResponse<KioskOrderRowDto> resp = kioskOrderService.queryGrid(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createOrder(@Valid @RequestBody KioskOrderUpsertDto dto) {
        Integer id = kioskOrderService.createOrder(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateOrder(
            @PathVariable Integer id,
            @Valid @RequestBody KioskOrderUpsertDto dto
    ) {
        kioskOrderService.updateOrder(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        kioskOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
