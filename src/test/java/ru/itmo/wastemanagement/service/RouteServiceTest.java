package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.KioskOrderRepository;
import ru.itmo.wastemanagement.repository.RouteRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    RouteRepository routeRepository;

    @Mock
    RouteStopRepository routeStopRepository;

    @Mock
    KioskOrderRepository kioskOrderRepository;

    @Mock
    GarbagePointRepository garbagePointRepository;

    @InjectMocks
    RouteService routeService;

    @Captor
    ArgumentCaptor<List<RouteStop>> stopsCaptor;

    private GarbagePoint gp(int id, Integer capacity, String address) {
        GarbagePoint gp = new GarbagePoint();
        gp.setId(id);
        gp.setCapacity(capacity);
        gp.setAddress(address);
        return gp;
    }

    @Test
    void autoGenerateCreatesRouteForFilledPoints() {
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route r = invocation.getArgument(0);
            if (r.getId() == null) {
                r.setId(100);
            }
            return r;
        });
        when(routeStopRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // weights from kiosk orders: gp1=70kg (cap 100 -> 70%), gp2=20kg (cap 200 -> 10%, skip), gp3=50kg (cap 60 -> 83%)
        when(kioskOrderRepository.findActiveWeightsByGarbagePoint()).thenReturn(List.of(
                new Object[]{1, 70d},
                new Object[]{2, 20d},
                new Object[]{3, 50d}
        ));

        when(garbagePointRepository.findById(1)).thenReturn(Optional.of(gp(1, 100, "Address 1")));
        when(garbagePointRepository.findById(2)).thenReturn(Optional.of(gp(2, 200, "Address 2")));
        when(garbagePointRepository.findById(3)).thenReturn(Optional.of(gp(3, 60, "Address 3")));

        RouteDto dto = routeService.autoGenerateFromKioskOrders();

        assertThat(dto.getId()).isEqualTo(100);
        assertThat(dto.getStatus()).isEqualTo(RouteStatus.planned);
        assertThat(dto.getPlannedDate()).isEqualTo(LocalDate.now());

        // only gp1 and gp3 exceed threshold
        assertThat(dto.getStops()).hasSize(2);
        assertThat(dto.getStops().stream().map(s -> s.getGarbagePointId()).toList())
                .containsExactlyInAnyOrder(1, 3);

        verify(routeStopRepository).saveAll(stopsCaptor.capture());
        List<RouteStop> savedStops = stopsCaptor.getValue();
        assertThat(savedStops).hasSize(2);
        assertThat(savedStops).allMatch(s -> s.getRoute().getId().equals(100));

        // expected capacity rounded from weight
        RouteStop stopForGp1 = savedStops.stream().filter(s -> s.getGarbagePoint().getId() == 1).findFirst().orElseThrow();
        assertThat(stopForGp1.getExpectedCapacity()).isEqualTo(70);
    }

    @Test
    void autoGenerateFailsWhenNoFilledPoints() {
        when(kioskOrderRepository.findActiveWeightsByGarbagePoint()).thenReturn(List.<Object[]>of(
                new Object[]{1, 5d} // capacity 100 => 5%
        ));
        when(garbagePointRepository.findById(1)).thenReturn(Optional.of(gp(1, 100, "Addr")));

        assertThatThrownBy(() -> routeService.autoGenerateFromKioskOrders())
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("70%");
    }
}
