package ru.itmo.wastemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;
import ru.itmo.wastemanagement.dto.incident.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.incident.IncidentDto;
import ru.itmo.wastemanagement.service.IncidentService;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public List<IncidentDto> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Integer id) {
        IncidentDto dto = incidentService.getIncidentById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/route/{routeId}")
    public List<IncidentDto> getIncidentsByRoute(@PathVariable Integer routeId) {
        return incidentService.getIncidentsByRouteId(routeId);
    }

    @PostMapping
    public ResponseEntity<IncidentDto> createIncident(
            @Valid @RequestBody IncidentCreateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String creatorLogin = userDetails != null ? userDetails.getUsername() : null;
        IncidentDto created = incidentService.createIncident(dto, creatorLogin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<IncidentDto> resolveIncident(@PathVariable Integer id) {
        IncidentDto resolved = incidentService.resolveIncident(id);
        return ResponseEntity.ok(resolved);
    }
}

