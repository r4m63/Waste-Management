package ru.itmo.wastemanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.dto.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.IncidentDto;
import ru.itmo.wastemanagement.service.IncidentService;

import java.util.List;

/**
 * Admin REST Controller для управления инцидентами.
 */
@RestController
@RequestMapping("/api/admin/incidents")
@RequiredArgsConstructor
public class AdminIncidentController {

    private final IncidentService incidentService;

    /**
     * Получить все инциденты.
     */
    @GetMapping
    public ResponseEntity<List<IncidentDto>> getAll() {
        List<IncidentDto> incidents = incidentService.findAll();
        return ResponseEntity.ok(incidents);
    }

    /**
     * Получить нерешенные инциденты.
     */
    @GetMapping("/unresolved")
    public ResponseEntity<List<IncidentDto>> getUnresolved() {
        List<IncidentDto> incidents = incidentService.findUnresolved();
        return ResponseEntity.ok(incidents);
    }

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

    /**
     * Пометить инцидент как решенный.
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<IncidentDto> resolve(@PathVariable Integer id) {
        IncidentDto resolved = incidentService.resolve(id);
        return ResponseEntity.ok(resolved);
    }

    /**
     * Удалить инцидент.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        incidentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

