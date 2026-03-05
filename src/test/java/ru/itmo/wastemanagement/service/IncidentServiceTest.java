package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.incident.IncidentCreateDto;
import ru.itmo.wastemanagement.entity.Incident;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.IncidentType;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.IncidentRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void getAllIncidentsReturnsDtos() {
        Incident i = incident(1, false);
        when(incidentRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(i));

        var result = incidentService.getAllIncidents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getIncidentByIdThrowsWhenNotFound() {
        when(incidentRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getIncidentById(7))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createIncidentValidatesRequiredFields() {
        IncidentCreateDto dto = new IncidentCreateDto();

        assertThatThrownBy(() -> incidentService.createIncident(dto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("stopId");

        dto.setStopId(1);

        assertThatThrownBy(() -> incidentService.createIncident(dto, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("type");
    }

    @Test
    void createIncidentSavesAndMapsCreatorIfPresent() {
        RouteStop stop = new RouteStop();
        stop.setId(4);
        stop.setAddress("Addr");
        Route route = new Route();
        route.setId(10);
        stop.setRoute(route);

        User creator = new User();
        creator.setId(2);
        creator.setLogin("driver");

        IncidentCreateDto dto = new IncidentCreateDto();
        dto.setStopId(4);
        dto.setType(IncidentType.overload);
        dto.setDescription("desc");

        when(routeStopRepository.findById(4)).thenReturn(Optional.of(stop));
        when(userRepository.findByLogin("driver")).thenReturn(Optional.of(creator));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident i = inv.getArgument(0);
            i.setId(44);
            return i;
        });

        var result = incidentService.createIncident(dto, "driver");

        assertThat(result.getId()).isEqualTo(44);
        assertThat(result.getCreatedById()).isEqualTo(2);
        assertThat(result.getStopId()).isEqualTo(4);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void resolveIncidentThrowsWhenAlreadyResolved() {
        Incident incident = incident(9, true);
        when(incidentRepository.findById(9)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.resolveIncident(9))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("уже решён");
    }

    @Test
    void resolveIncidentMarksResolvedAndSaves() {
        Incident incident = incident(9, false);
        when(incidentRepository.findById(9)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(incident)).thenReturn(incident);

        var result = incidentService.resolveIncident(9);

        assertThat(incident.isResolved()).isTrue();
        assertThat(incident.getResolvedAt()).isNotNull();
        assertThat(incident.getUpdatedAt()).isNotNull();
        assertThat(result.isResolved()).isTrue();
        verify(incidentRepository).save(incident);
    }

    private static Incident incident(int id, boolean resolved) {
        Route route = new Route();
        route.setId(1);
        RouteStop stop = new RouteStop();
        stop.setId(2);
        stop.setRoute(route);

        Incident i = new Incident();
        i.setId(id);
        i.setStop(stop);
        i.setType(IncidentType.overload);
        i.setResolved(resolved);
        i.setCreatedAt(java.time.LocalDateTime.now());
        i.setUpdatedAt(java.time.LocalDateTime.now());
        return i;
    }
}
