package ru.itmo.wastemanagement.dto;

import org.junit.jupiter.api.Test;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeRowDto;
import ru.itmo.wastemanagement.dto.driver.DriverRowDto;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointCreateUpdateDto;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointRowDto;
import ru.itmo.wastemanagement.dto.incident.IncidentDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskRowDto;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderRowDto;
import ru.itmo.wastemanagement.dto.route.RouteStopDto;
import ru.itmo.wastemanagement.dto.shift.DriverShiftDto;
import ru.itmo.wastemanagement.dto.vehicle.VehicleRowDto;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.Incident;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.entity.enums.IncidentType;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMappingTest {

    @Test
    void rowDtosMapFromEntity() {
        LocalDateTime now = LocalDateTime.now();

        ContainerSize cs = new ContainerSize();
        cs.setId(1L);
        cs.setCode("M");
        cs.setCreatedAt(now);

        User user = new User();
        user.setId(2);
        user.setName("User");
        user.setLogin("u");
        user.setPhone("p");
        user.setActive(true);
        user.setCreatedAt(now);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3);
        vehicle.setPlateNumber("A123");
        vehicle.setCreatedAt(now);

        assertThat(ContainerSizeRowDto.fromEntity(cs).getId()).isEqualTo(1L);
        assertThat(DriverRowDto.fromEntity(user).getId()).isEqualTo(2);
        assertThat(KioskRowDto.fromEntity(user).getLogin()).isEqualTo("u");
        assertThat(VehicleRowDto.fromEntity(vehicle).getPlateNumber()).isEqualTo("A123");
        assertThat(DriverRowDto.fromEntity(null)).isNull();
        assertThat(KioskRowDto.fromEntity(null)).isNull();
        assertThat(VehicleRowDto.fromEntity(null)).isNull();
    }

    @Test
    void garbagePointDtoAndEntityConversionWorks() {
        User admin = new User();
        admin.setId(10);
        User kiosk = new User();
        kiosk.setId(11);

        GarbagePoint gp = new GarbagePoint();
        gp.setId(5);
        gp.setAddress("Addr");
        gp.setCapacity(100);
        gp.setOpen(true);
        gp.setLat(59.1);
        gp.setLon(30.1);
        gp.setAdmin(admin);
        gp.setKiosk(kiosk);

        GarbagePointRowDto rowDto = GarbagePointRowDto.fromEntity(gp);
        assertThat(rowDto.getId()).isEqualTo(5L);
        assertThat(rowDto.getAdminId()).isEqualTo(10L);
        assertThat(rowDto.getKioskId()).isEqualTo(11L);
        assertThat(GarbagePointRowDto.fromEntity(null)).isNull();

        GarbagePointCreateUpdateDto dto = GarbagePointCreateUpdateDto.toDto(gp);
        assertThat(dto.getAddress()).isEqualTo("Addr");
        assertThat(dto.getKioskId()).isEqualTo(11);

        GarbagePoint created = GarbagePointCreateUpdateDto.toEntity(dto, null);
        assertThat(created.getAddress()).isEqualTo("Addr");

        dto.setOpen(null);
        GarbagePoint updated = new GarbagePoint();
        GarbagePointCreateUpdateDto.toEntity(dto, updated);
        assertThat(updated.isOpen()).isTrue();
    }

    @Test
    void incidentRouteStopShiftAndOrderDtosMapNestedEntities() {
        User creator = new User();
        creator.setId(1);
        creator.setName("Driver");
        creator.setLogin("driver");

        Route route = new Route();
        route.setId(7);

        RouteStop stop = new RouteStop();
        stop.setId(8);
        stop.setSeqNo(1);
        stop.setAddress("Stop");
        stop.setRoute(route);

        Incident incident = new Incident();
        incident.setId(9);
        incident.setStop(stop);
        incident.setCreatedBy(creator);
        incident.setType(IncidentType.traffic);
        incident.setResolved(false);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        IncidentDto incidentDto = IncidentDto.fromEntity(incident);
        assertThat(incidentDto.getRouteId()).isEqualTo(7);
        assertThat(incidentDto.getCreatedByLogin()).isEqualTo("driver");
        assertThat(IncidentDto.fromEntity(null)).isNull();

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3);
        vehicle.setPlateNumber("A111");
        DriverShift shift = new DriverShift();
        shift.setId(2);
        shift.setDriver(creator);
        shift.setVehicle(vehicle);
        shift.setStatus(ShiftStatus.open);

        DriverShiftDto shiftDto = DriverShiftDto.fromEntity(shift);
        assertThat(shiftDto.getDriverId()).isEqualTo(1);
        assertThat(shiftDto.getVehiclePlate()).isEqualTo("A111");
        assertThat(DriverShiftDto.fromEntity(null)).isNull();

        GarbagePoint gp = new GarbagePoint();
        gp.setId(4);
        stop.setGarbagePoint(gp);
        RouteStopDto stopDto = RouteStopDto.fromEntity(stop);
        assertThat(stopDto.getGarbagePointId()).isEqualTo(4);

        Fraction fraction = new Fraction();
        fraction.setId(6);
        fraction.setName("Glass");

        ContainerSize cs = new ContainerSize();
        cs.setId(10L);
        cs.setCode("M");

        KioskOrder order = new KioskOrder();
        order.setId(1);
        order.setGarbagePoint(gp);
        order.setContainerSize(cs);
        order.setFraction(fraction);
        order.setUser(creator);
        order.setStatus(OrderStatus.CONFIRMED);

        KioskOrderRowDto orderDto = KioskOrderRowDto.fromEntity(order);
        assertThat(orderDto.getContainerSizeId()).isEqualTo(10L);
        assertThat(orderDto.getFractionName()).isEqualTo("Glass");
        assertThat(KioskOrderRowDto.fromEntity(null)).isNull();
    }
}
