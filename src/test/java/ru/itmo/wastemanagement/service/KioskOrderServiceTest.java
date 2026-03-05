package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderUpsertDto;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.ContainerSizeRepository;
import ru.itmo.wastemanagement.repository.FractionRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.KioskOrderGridRepository;
import ru.itmo.wastemanagement.repository.KioskOrderRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KioskOrderServiceTest {

    @Mock
    private KioskOrderRepository kioskOrderRepository;

    @Mock
    private KioskOrderGridRepository kioskOrderGridRepository;

    @Mock
    private GarbagePointRepository garbagePointRepository;

    @Mock
    private ContainerSizeRepository containerSizeRepository;

    @Mock
    private FractionRepository fractionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private KioskOrderService kioskOrderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void queryGridReturnsMappedRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        KioskOrder row = new KioskOrder();
        row.setId(1);
        when(kioskOrderGridRepository.findPageByGrid(req)).thenReturn(List.of(row));
        when(kioskOrderGridRepository.countByGrid(req)).thenReturn(1L);

        var result = kioskOrderService.queryGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getId()).isEqualTo(1);
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createOrderWithExplicitUserAndPointReturnsId() {
        KioskOrderUpsertDto dto = dto();
        dto.setUserId(9);
        dto.setGarbagePointId(4);

        User user = new User();
        user.setId(9);
        user.setRole(UserRole.KIOSK);
        GarbagePoint gp = new GarbagePoint();
        gp.setId(4);

        when(userRepository.findById(9)).thenReturn(Optional.of(user));
        when(garbagePointRepository.findById(4)).thenReturn(Optional.of(gp));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(4), eq(dto.getContainerSizeId()), eq(9), eq(dto.getFractionId())))
                .thenReturn(123);

        Integer id = kioskOrderService.createOrder(dto);

        assertThat(id).isEqualTo(123);
    }

    @Test
    void createOrderThrowsTranslatedErrorForClosedPoint() {
        KioskOrderUpsertDto dto = dto();
        dto.setUserId(9);
        dto.setGarbagePointId(4);

        User user = new User();
        user.setId(9);
        user.setRole(UserRole.KIOSK);
        GarbagePoint gp = new GarbagePoint();
        gp.setId(4);

        when(userRepository.findById(9)).thenReturn(Optional.of(user));
        when(garbagePointRepository.findById(4)).thenReturn(Optional.of(gp));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(4), eq(dto.getContainerSizeId()), eq(9), eq(dto.getFractionId())))
                .thenThrow(new DataAccessResourceFailureException("Garbage point not found or closed"));

        assertThatThrownBy(() -> kioskOrderService.createOrder(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не найдена или закрыта");
    }

    @Test
    void createOrderThrowsWhenExplicitUserRoleInvalid() {
        KioskOrderUpsertDto dto = dto();
        dto.setUserId(1);
        dto.setGarbagePointId(4);

        User user = new User();
        user.setRole(UserRole.DRIVER);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> kioskOrderService.createOrder(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не может оформлять");
    }

    @Test
    void createOrderResolvesUserFromSecurityContextWhenNoUserId() {
        User principalUser = new User();
        principalUser.setId(77);
        principalUser.setRole(UserRole.KIOSK);
        principalUser.setLogin("kiosk");
        SecurityContextHolder.getContext().setAuthentication(auth(principalUser));

        when(userRepository.findById(77)).thenReturn(Optional.of(principalUser));

        GarbagePoint gp = new GarbagePoint();
        gp.setId(8);
        when(garbagePointRepository.findFirstByKiosk_Id(77)).thenReturn(Optional.of(gp));
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq(8), eq(10), eq(77), eq(11)))
                .thenReturn(33);

        KioskOrderUpsertDto dto = new KioskOrderUpsertDto();
        dto.setContainerSizeId(10);
        dto.setFractionId(11);

        Integer id = kioskOrderService.createOrder(dto);

        assertThat(id).isEqualTo(33);
    }

    @Test
    void updateOrderThrowsWhenOrderNotFound() {
        when(kioskOrderRepository.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kioskOrderService.updateOrder(9, dto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOrderUpdatesAllReferencesAndStatus() {
        KioskOrder order = new KioskOrder();
        when(kioskOrderRepository.findById(5)).thenReturn(Optional.of(order));

        KioskOrderUpsertDto dto = dto();
        dto.setUserId(1);
        dto.setGarbagePointId(2);
        dto.setContainerSizeId(3);
        dto.setFractionId(4);
        dto.setWeight(15.0);
        dto.setStatus(OrderStatus.CANCELLED);

        User user = new User();
        user.setId(1);
        user.setRole(UserRole.KIOSK);
        GarbagePoint gp = new GarbagePoint();
        gp.setId(2);
        ContainerSize cs = new ContainerSize();
        cs.setId(3L);
        Fraction fraction = new Fraction();
        fraction.setId(4);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(garbagePointRepository.findById(2)).thenReturn(Optional.of(gp));
        when(containerSizeRepository.findById(3L)).thenReturn(Optional.of(cs));
        when(fractionRepository.findById(4)).thenReturn(Optional.of(fraction));

        kioskOrderService.updateOrder(5, dto);

        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getGarbagePoint()).isEqualTo(gp);
        assertThat(order.getContainerSize()).isEqualTo(cs);
        assertThat(order.getFraction()).isEqualTo(fraction);
        assertThat(order.getWeight()).isEqualTo(15.0);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(kioskOrderRepository).save(order);
    }

    @Test
    void deleteOrderDeletesEntity() {
        KioskOrder order = new KioskOrder();
        when(kioskOrderRepository.findById(6)).thenReturn(Optional.of(order));

        kioskOrderService.deleteOrder(6);

        verify(kioskOrderRepository).delete(order);
    }

    private static Authentication auth(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        return authentication;
    }

    private static KioskOrderUpsertDto dto() {
        KioskOrderUpsertDto dto = new KioskOrderUpsertDto();
        dto.setContainerSizeId(10);
        dto.setFractionId(11);
        dto.setWeight(2.0);
        return dto;
    }
}
