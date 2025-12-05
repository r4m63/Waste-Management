package ru.itmo.wastemanagement.controller.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.IncidentDto;
import ru.itmo.wastemanagement.service.IncidentService;

import java.util.List;

/**
 * REST Controller для управления инцидентами водителя.
 * Эндпоинты для driver-frontend.
 */
@RestController
@RequestMapping("/api/driver/incidents")
@RequiredArgsConstructor
public class DriverIncidentController {

    private final IncidentService incidentService;

    /**
     * Получить инциденты по остановке.
     */
    @GetMapping("/stop/{stopId}")
    public ResponseEntity<List<IncidentDto>> getByStop(@PathVariable Integer stopId) {
        List<IncidentDto> incidents = incidentService.findByStop(stopId);
        return ResponseEntity.ok(incidents);
    }

    /**
     * Получить инцидент по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentDto> getById(@PathVariable Integer id) {
        IncidentDto incident = incidentService.findById(id);
        return ResponseEntity.ok(incident);
    }

    /**
     * Создать инцидент.
     */
    @PostMapping
    public ResponseEntity<IncidentDto> create(@RequestBody IncidentCreateDto dto) {
        IncidentDto created = incidentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить инцидент.
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncidentDto> update(@PathVariable Integer id, @RequestBody IncidentCreateDto dto) {
        IncidentDto updated = incidentService.update(id, dto);
        return ResponseEntity.ok(updated);
    }
}

