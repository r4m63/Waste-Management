package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskRowDto;
import ru.itmo.wastemanagement.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class KioskController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Integer> createKioskUser(@RequestBody @Valid KioskCreateDto dto) {
        Integer id = userService.createKioskUser(dto);
        return Map.of("id", id);
    }

    @PostMapping("/query")
    public GridTableResponse<KioskRowDto> query(@Valid @RequestBody GridTableRequest req) {
        return userService.queryKioskGrid(req);
    }

}
