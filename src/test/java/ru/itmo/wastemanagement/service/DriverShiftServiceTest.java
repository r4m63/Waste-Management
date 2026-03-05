package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.entity.enums.StopStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.DriverShiftRepository;
import ru.itmo.wastemanagement.repository.RouteRepository;
import ru.itmo.wastemanagement.repository.RouteStopRepository;
import ru.itmo.wastemanagement.repository.UserRepository;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverShiftServiceTest {

    @Mock
    private DriverShiftRepository driverShiftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @InjectMocks
    private DriverShiftService driverShiftService;

    @Test
    void getCurrentShiftByDriverLoginReturnsEmptyForBlankLogin() {
        assertThat(driverShiftService.getCurrentShiftByDriverLogin(" ")).isEmpty();
    }

    @Test
    void hasOpenShiftReturnsFalseForUnknownDriver() {
        when(userRepository.findByLogin("driver")).thenReturn(Optional.empty());

        assertThat(driverShiftService.hasOpenShift("driver")).isFalse();
    }

    @Test
    void openShiftThrowsWhenLoginMissing() {
        assertThatThrownBy(() -> driverShiftService.openShift(" ", null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void openShiftThrowsWhenUserNotDriver() {
        User user = new User();
        user.setRole(UserRole.ADMIN);
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> driverShiftService.openShift("admin", null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void openShiftThrowsWhenAlreadyHasOpenShift() {
        User driver = driver(1);
        when(userRepository.findByLogin("d")).thenReturn(Optional.of(driver));
        when(driverShiftRepository.existsByDriver_IdAndStatus(1, ShiftStatus.open)).thenReturn(true);

        assertThatThrownBy(() -> driverShiftService.openShift("d", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("уже есть открытая");
    }

    @Test
    void openShiftThrowsWhenVehicleNotFound() {
        User driver = driver(1);
        when(userRepository.findByLogin("d")).thenReturn(Optional.of(driver));
        when(driverShiftRepository.existsByDriver_IdAndStatus(1, ShiftStatus.open)).thenReturn(false);
        when(vehicleRepository.findById(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverShiftService.openShift("d", 77))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void openShiftSavesAndReturnsDto() {
        User driver = driver(1);
        Vehicle vehicle = new Vehicle();
        vehicle.setId(7);

        when(userRepository.findByLogin("d")).thenReturn(Optional.of(driver));
        when(driverShiftRepository.existsByDriver_IdAndStatus(1, ShiftStatus.open)).thenReturn(false);
        when(vehicleRepository.findById(7)).thenReturn(Optional.of(vehicle));
        when(driverShiftRepository.save(any(DriverShift.class))).thenAnswer(inv -> {
            DriverShift s = inv.getArgument(0);
            s.setId(42);
            return s;
        });

        var dto = driverShiftService.openShift("d", 7);

        assertThat(dto.getId()).isEqualTo(42);
        assertThat(dto.getDriverId()).isEqualTo(1);
        assertThat(dto.getVehicleId()).isEqualTo(7);
        assertThat(dto.getStatus()).isEqualTo(ShiftStatus.open);
    }

    @Test
    void closeShiftThrowsWhenShiftNotFound() {
        when(driverShiftRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverShiftService.closeShift(9, "d"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void closeShiftThrowsWhenDriverMismatch() {
        DriverShift shift = new DriverShift();
        User shiftDriver = driver(1);
        shift.setDriver(shiftDriver);
        shift.setStatus(ShiftStatus.open);
        when(driverShiftRepository.findById(3)).thenReturn(Optional.of(shift));

        User another = driver(2);
        when(userRepository.findByLogin("d")).thenReturn(Optional.of(another));

        assertThatThrownBy(() -> driverShiftService.closeShift(3, "d"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не принадлежит");
    }

    @Test
    void closeShiftThrowsWhenAlreadyClosed() {
        DriverShift shift = new DriverShift();
        shift.setStatus(ShiftStatus.closed);
        when(driverShiftRepository.findById(3)).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> driverShiftService.closeShift(3, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("уже закрыта");
    }

    @Test
    void closeShiftThrowsWhenActiveRoutesExist() {
        DriverShift shift = new DriverShift();
        shift.setStatus(ShiftStatus.open);
        when(driverShiftRepository.findById(3)).thenReturn(Optional.of(shift));
        when(routeRepository.findByShift_IdAndStatusIn(3, List.of(RouteStatus.planned, RouteStatus.in_progress)))
                .thenReturn(List.of(new Route()));

        assertThatThrownBy(() -> driverShiftService.closeShift(3, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("активные маршруты");
    }

    @Test
    void closeShiftThrowsWhenIncompleteStopsExist() {
        DriverShift shift = new DriverShift();
        shift.setStatus(ShiftStatus.open);
        when(driverShiftRepository.findById(3)).thenReturn(Optional.of(shift));
        when(routeRepository.findByShift_IdAndStatusIn(3, List.of(RouteStatus.planned, RouteStatus.in_progress)))
                .thenReturn(List.of());

        Route route = new Route();
        route.setId(100);
        when(routeRepository.findByShift_IdAndStatusIn(3, List.of(RouteStatus.planned, RouteStatus.in_progress, RouteStatus.completed)))
                .thenReturn(List.of(route));

        RouteStop stop = new RouteStop();
        stop.setStatus(StopStatus.planned);
        when(routeStopRepository.findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(List.of(100))).thenReturn(List.of(stop));

        assertThatThrownBy(() -> driverShiftService.closeShift(3, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("незавершенных остановок");
    }

    @Test
    void closeShiftClosesWhenNoActiveRoutesAndAllStopsComplete() {
        DriverShift shift = new DriverShift();
        shift.setStatus(ShiftStatus.open);
        when(driverShiftRepository.findById(3)).thenReturn(Optional.of(shift));
        when(routeRepository.findByShift_IdAndStatusIn(3, List.of(RouteStatus.planned, RouteStatus.in_progress)))
                .thenReturn(List.of());
        when(routeRepository.findByShift_IdAndStatusIn(3, List.of(RouteStatus.planned, RouteStatus.in_progress, RouteStatus.completed)))
                .thenReturn(List.of());
        when(driverShiftRepository.save(shift)).thenReturn(shift);

        var dto = driverShiftService.closeShift(3, null);

        assertThat(shift.getStatus()).isEqualTo(ShiftStatus.closed);
        assertThat(shift.getClosedAt()).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(ShiftStatus.closed);
        verify(driverShiftRepository).save(shift);
    }

    private static User driver(int id) {
        User u = new User();
        u.setId(id);
        u.setRole(UserRole.DRIVER);
        u.setLogin("d" + id);
        return u;
    }
}
