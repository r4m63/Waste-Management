package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.repository.ContainerSizeRepository;
import ru.itmo.wastemanagement.repository.FractionRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.KioskOrderRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RouteServiceIntegrationTest {

    @Autowired
    private RouteService routeService;

    @Autowired
    private GarbagePointRepository garbagePointRepository;

    @Autowired
    private ContainerSizeRepository containerSizeRepository;

    @Autowired
    private FractionRepository fractionRepository;

    @Autowired
    private KioskOrderRepository kioskOrderRepository;

    @Test
    @Transactional
    void autoGenerateCreatesRouteWithFilledPoints() {
        // справочники (делаем уникальные значения, чтобы тест не падал при повторах)
        String suffix = String.valueOf(System.nanoTime());

        ContainerSize cs = containerSizeRepository.save(ContainerSize.builder()
                .code("M-" + suffix)     // если у тебя code UNIQUE
                .capacity(100)
                .length(80d)
                .width(60d)
                .height(60d)
                .description("Test size")
                .createdAt(LocalDateTime.now())
                .build());

        Fraction fraction = fractionRepository.save(Fraction.builder()
                .name("Mixed-" + suffix) // если name UNIQUE
                .code("MIX-" + suffix)   // если code UNIQUE
                .description("Test fraction")
                .hazardous(false)
                .build());

        // точки
        GarbagePoint gp1 = garbagePointRepository.save(GarbagePoint.builder()
                .address("Addr 1 " + suffix) // address у тебя не unique, но пусть будет уникально
                .capacity(100)
                .open(true)
                .createdAt(LocalDateTime.now())
                .build());

        GarbagePoint gp2 = garbagePointRepository.save(GarbagePoint.builder()
                .address("Addr 2 " + suffix)
                .capacity(300)
                .open(true)
                .createdAt(LocalDateTime.now())
                .build());

        // заявки киоска
        kioskOrderRepository.save(KioskOrder.builder()
                .garbagePoint(gp1)
                .containerSize(cs)
                .fraction(fraction)
                .weight(80d)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.CONFIRMED)
                .build());

        kioskOrderRepository.save(KioskOrder.builder()
                .garbagePoint(gp2)
                .containerSize(cs)
                .fraction(fraction)
                .weight(60d)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.CONFIRMED)
                .build());

        RouteDto dto = routeService.autoGenerateFromKioskOrders();

        assertThat(dto.getStops()).hasSize(1);
        assertThat(dto.getStops().get(0).getGarbagePointId()).isEqualTo(gp1.getId());
        assertThat(dto.getStops().get(0).getExpectedCapacity()).isEqualTo(80);
    }
}
