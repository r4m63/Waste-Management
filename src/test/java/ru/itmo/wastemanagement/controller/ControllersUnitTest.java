package ru.itmo.wastemanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;
import ru.itmo.wastemanagement.dto.AuthRequest;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeUpsertDto;
import ru.itmo.wastemanagement.dto.driver.DriverCreateUpdateDto;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointCreateUpdateDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.incident.IncidentCreateDto;
import ru.itmo.wastemanagement.dto.incident.IncidentDto;
import ru.itmo.wastemanagement.dto.kiosk.KioskCreateUpdateDto;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderUpsertDto;
import ru.itmo.wastemanagement.dto.route.RouteAssignDto;
import ru.itmo.wastemanagement.dto.route.RouteDto;
import ru.itmo.wastemanagement.dto.route.RouteStopUpdateDto;
import ru.itmo.wastemanagement.dto.shift.DriverShiftDto;
import ru.itmo.wastemanagement.dto.shift.ShiftOpenDto;
import ru.itmo.wastemanagement.dto.vehicle.VehicleUpsertDto;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.service.ContainerSizeService;
import ru.itmo.wastemanagement.service.DriverService;
import ru.itmo.wastemanagement.service.DriverShiftService;
import ru.itmo.wastemanagement.service.FractionService;
import ru.itmo.wastemanagement.service.GarbagePointService;
import ru.itmo.wastemanagement.service.IncidentService;
import ru.itmo.wastemanagement.service.KioskOrderService;
import ru.itmo.wastemanagement.service.KioskService;
import ru.itmo.wastemanagement.service.RouteService;
import ru.itmo.wastemanagement.service.VehicleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllersUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authControllerLoginSuccessAndMeAndLogout() {
        AuthController controller = new AuthController(authenticationManager);

        User user = new User();
        user.setId(5);
        user.setLogin("admin");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        CustomUserDetails cud = new CustomUserDetails(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(cud);

        when(authenticationManager.authenticate(any())).thenReturn(auth);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);

        AuthRequest req = AuthRequest.builder().login("admin").password("pwd").build();
        ResponseEntity<?> response = controller.authenticate(req, request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<?> me = controller.currentUser();
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);

        when(request.getSession(false)).thenReturn(session);
        ResponseEntity<?> logout = controller.logout(request);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(session).invalidate();
    }

    @Test
    void authControllerLoginFailureReturnsUnauthorized() {
        AuthController controller = new AuthController(authenticationManager);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        AuthRequest req = AuthRequest.builder().login("x").password("y").build();
        ResponseEntity<?> response = controller.authenticate(req, mock(HttpServletRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void routeControllerUnauthorizedBranches() {
        RouteService routeService = mock(RouteService.class);
        RouteController c = new RouteController(routeService);

        assertThat(c.getMyRoutes(null)).isEmpty();
        assertThat(c.getMyRoute(1, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.startRoute(1, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.acceptRoute(1, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.finishRoute(1, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.updateStop(1, 2, new RouteStopUpdateDto(), null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void routeControllerDelegatesToService() {
        RouteService routeService = mock(RouteService.class);
        RouteController c = new RouteController(routeService);
        CustomUserDetails user = principal(10, "driver", UserRole.DRIVER);

        RouteDto dto = RouteDto.builder().id(1).build();
        when(routeService.getAllRoutesWithStops()).thenReturn(List.of(dto));
        when(routeService.getRoutesForDriverLogin("driver")).thenReturn(List.of(dto));
        when(routeService.getMyRoute(1, "driver")).thenReturn(dto);
        when(routeService.autoGenerateFromKioskOrders()).thenReturn(List.of(dto));
        when(routeService.assignDriver(eq(1), eq(2), any(), any())).thenReturn(dto);
        when(routeService.startRoute(1, "driver")).thenReturn(dto);
        when(routeService.updateStop(eq(1), eq(2), eq("driver"), any())).thenReturn(dto);
        when(routeService.finishRoute(1, "driver")).thenReturn(dto);

        assertThat(c.getRoutes()).hasSize(1);
        assertThat(c.getMyRoutes(user)).hasSize(1);
        assertThat(c.getMyRoute(1, user).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.autoGenerate().getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(c.assignDriver(1, new RouteAssignDto() {{ setDriverId(2); }}).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.startRoute(1, user).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.acceptRoute(1, user).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.updateStop(1, 2, new RouteStopUpdateDto(), user).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.finishRoute(1, user).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(c.deleteRoute(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(routeService).deleteRoute(1);
    }

    @Test
    void incidentControllerDelegatesToService() {
        IncidentService service = mock(IncidentService.class);
        IncidentController c = new IncidentController(service);
        IncidentDto dto = IncidentDto.builder().id(1).build();
        when(service.getAllIncidents()).thenReturn(List.of(dto));
        when(service.getIncidentById(1)).thenReturn(dto);
        when(service.getIncidentsByRouteId(3)).thenReturn(List.of(dto));
        when(service.createIncident(any(), eq("driver"))).thenReturn(dto);
        when(service.resolveIncident(1)).thenReturn(dto);

        assertThat(c.getAllIncidents()).hasSize(1);
        assertThat(c.getIncidentById(1).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.getIncidentsByRoute(3)).hasSize(1);

        CustomUserDetails user = principal(1, "driver", UserRole.DRIVER);
        assertThat(c.createIncident(new IncidentCreateDto(), user).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(c.resolveIncident(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shiftsControllerHandlesUnauthorizedAndSuccess() {
        DriverShiftService service = mock(DriverShiftService.class);
        DriverShiftController c = new DriverShiftController(service);

        assertThat(c.getCurrentShift(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.openShift(new ShiftOpenDto(), null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(c.closeShift(1, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        DriverShiftDto dto = DriverShiftDto.builder().id(1).build();
        when(service.getAllShifts()).thenReturn(List.of(dto));
        when(service.getShiftsByDriverId(7)).thenReturn(List.of(dto));
        when(service.getCurrentShiftByDriverLogin("driver")).thenReturn(Optional.of(dto));
        when(service.openShift("driver", 2)).thenReturn(dto);
        when(service.closeShift(1, "driver")).thenReturn(dto);

        CustomUserDetails user = principal(9, "driver", UserRole.DRIVER);
        ShiftOpenDto openDto = new ShiftOpenDto();
        openDto.setVehicleId(2);

        assertThat(c.getAllShifts()).hasSize(1);
        assertThat(c.getShiftsByDriver(7)).hasSize(1);
        assertThat(c.getCurrentShift(user).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.openShift(openDto, user).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(c.closeShift(1, user).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void crudControllersReturnExpectedStatuses() {
        FractionService fractionService = mock(FractionService.class);
        when(fractionService.createFraction(any())).thenReturn(1);
        FractionController fractionController = new FractionController(fractionService);

        DriverService driverService = mock(DriverService.class);
        when(driverService.createDriver(any())).thenReturn(1);
        DriverController driverController = new DriverController(driverService);

        VehicleService vehicleService = mock(VehicleService.class);
        when(vehicleService.createVehicle(any())).thenReturn(1);
        VehicleController vehicleController = new VehicleController(vehicleService);

        KioskService kioskService = mock(KioskService.class);
        when(kioskService.createKioskUser(any())).thenReturn(1);
        KioskController kioskController = new KioskController(kioskService);

        KioskOrderService kioskOrderService = mock(KioskOrderService.class);
        when(kioskOrderService.createOrder(any())).thenReturn(1);
        KioskOrderController orderController = new KioskOrderController(kioskOrderService);

        ContainerSizeService containerSizeService = mock(ContainerSizeService.class);
        when(containerSizeService.createContainerSize(any())).thenReturn(1L);
        ContainerSizeController containerController = new ContainerSizeController(containerSizeService);

        GarbagePointService garbagePointService = mock(GarbagePointService.class);
        when(garbagePointService.createNewGarbagePoint(any())).thenReturn(1);
        GarbagePointController gpController = new GarbagePointController(garbagePointService);

        assertThat(fractionController.createFraction(new ru.itmo.wastemanagement.dto.fraction.FractionUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(fractionController.updateFraction(1, new ru.itmo.wastemanagement.dto.fraction.FractionUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(fractionController.deleteFraction(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(driverController.createDriver(new DriverCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(driverController.updateDriver(1, new DriverCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(driverController.deleteDriver(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(vehicleController.createVehicle(new VehicleUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(vehicleController.updateVehicle(1, new VehicleUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(vehicleController.deleteVehicle(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(kioskController.createKioskUser(new KioskCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(kioskController.updateKioskUser(1, new KioskCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(kioskController.deleteKioskUser(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(orderController.createOrder(new KioskOrderUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderController.updateOrder(1, new KioskOrderUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(orderController.deleteOrder(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(containerController.createContainerSize(new ContainerSizeUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(containerController.updateContainerSize(1L, new ContainerSizeUpsertDto()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(containerController.deleteContainerSize(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(gpController.createGarbagePoint(new GarbagePointCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(gpController.update(1, new GarbagePointCreateUpdateDto()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(gpController.delete(1).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void authControllerMeWhenNoAuth() {
        AuthController controller = new AuthController(authenticationManager);
        ResponseEntity<?> me = controller.currentUser();
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(((Map<?, ?>) me.getBody()).get("error")).isEqualTo("Not authenticated");
    }

    private static CustomUserDetails principal(int id, String login, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setRole(role);
        user.setActive(true);
        return new CustomUserDetails(user);
    }
}
