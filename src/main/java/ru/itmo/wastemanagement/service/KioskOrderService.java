package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.config.security.CustomUserDetails;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderRowDto;
import ru.itmo.wastemanagement.dto.kioskorder.KioskOrderUpsertDto;
import ru.itmo.wastemanagement.entity.*;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KioskOrderService {

    private final KioskOrderRepository kioskOrderRepository;
    private final KioskOrderGridRepository kioskOrderGridRepository;

    private final GarbagePointRepository garbagePointRepository;
    private final ContainerSizeRepository containerSizeRepository;
    private final FractionRepository fractionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GridTableResponse<KioskOrderRowDto> queryGrid(GridTableRequest req) {
        List<KioskOrder> rows = kioskOrderGridRepository.findPageByGrid(req);
        long total = kioskOrderGridRepository.countByGrid(req);

        List<KioskOrderRowDto> dtos = rows.stream()
                .map(KioskOrderRowDto::fromEntity)
                .toList();

        return GridTableResponse.<KioskOrderRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createOrder(KioskOrderUpsertDto dto) {
        User user = resolveOrderUser(dto);

        GarbagePoint gp = resolveGarbagePoint(dto.getGarbagePointId(), user);

        ContainerSize cs = containerSizeRepository.findById(Long.valueOf(dto.getContainerSizeId()))
                .orElseThrow(() -> ResourceNotFoundException.of(
                        ContainerSize.class, "id", dto.getContainerSizeId()
                ));

        Fraction fraction = fractionRepository.findById(dto.getFractionId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        Fraction.class, "id", dto.getFractionId()
                ));

        OrderStatus status = dto.getStatus() != null
                ? dto.getStatus()
                : OrderStatus.CREATED;

        KioskOrder order = KioskOrder.builder()
                .garbagePoint(gp)
                .containerSize(cs)
                .user(user)
                .fraction(fraction)
                .weight(dto.getWeight())
                .createdAt(LocalDateTime.now())
                .status(status)
                .build();

        return kioskOrderRepository.save(order).getId();
    }

    @Transactional
    public void updateOrder(Integer id, KioskOrderUpsertDto dto) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(KioskOrder.class, "id", id));

        User user = resolveOrderUser(dto);

        GarbagePoint gp = resolveGarbagePoint(dto.getGarbagePointId(), user);

        ContainerSize cs = containerSizeRepository.findById(Long.valueOf(dto.getContainerSizeId()))
                .orElseThrow(() -> ResourceNotFoundException.of(
                        ContainerSize.class, "id", dto.getContainerSizeId()
                ));

        Fraction fraction = fractionRepository.findById(dto.getFractionId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        Fraction.class, "id", dto.getFractionId()
                ));

        order.setGarbagePoint(gp);
        order.setContainerSize(cs);
        order.setFraction(fraction);
        order.setUser(user);
        order.setWeight(dto.getWeight());

        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }

        kioskOrderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(KioskOrder.class, "id", id));

        kioskOrderRepository.delete(order);
    }

    private User resolveOrderUser(KioskOrderUpsertDto dto) {
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of(
                            User.class, "id", dto.getUserId()
                    ));

            if (user.getRole() != UserRole.KIOSK && user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException(
                        "Пользователь id=%d не может оформлять заказ киоска".formatted(dto.getUserId())
                );
            }
            return user;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            Integer userId = principal.getId();
            return userRepository.findById(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of(User.class, "id", userId));
        }

        return null;
    }

    private GarbagePoint resolveGarbagePoint(Integer garbagePointId, User kioskUser) {
        if (garbagePointId != null) {
            return garbagePointRepository.findById(garbagePointId)
                    .orElseThrow(() -> ResourceNotFoundException.of(
                            GarbagePoint.class, "id", garbagePointId
                    ));
        }

        if (kioskUser != null) {
            return garbagePointRepository.findFirstByKiosk_Id(kioskUser.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "Не найдено точки сбора для киоска id=%d".formatted(kioskUser.getId())
                    ));
        }

        throw new BadRequestException("Не удалось определить точку сбора: передайте garbagePointId или выполните запрос от имени киоска");
    }
}
