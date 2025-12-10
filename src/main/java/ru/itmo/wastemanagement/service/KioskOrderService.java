package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        GarbagePoint gp = garbagePointRepository.findById(dto.getGarbagePointId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        GarbagePoint.class, "id", dto.getGarbagePointId()
                ));

        ContainerSize cs = containerSizeRepository.findById(dto.getContainerSizeId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        ContainerSize.class, "id", dto.getContainerSizeId()
                ));

        Fraction fraction = fractionRepository.findById(dto.getFractionId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        Fraction.class, "id", dto.getFractionId()
                ));

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of(
                            User.class, "id", dto.getUserId()
                    ));

            if (user.getRole() != UserRole.KIOSK && user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException(
                        "Пользователь id=%d не может оформлять заказ киоска".formatted(dto.getUserId())
                );
            }
        }

        OrderStatus status = dto.getStatus() != null
                ? dto.getStatus()
                : OrderStatus.CREATED;

        KioskOrder order = KioskOrder.builder()
                .garbagePoint(gp)
                .containerSize(cs)
                .user(user)
                .fraction(fraction)
                .createdAt(LocalDateTime.now())
                .status(status)
                .build();

        return kioskOrderRepository.save(order).getId();
    }

    @Transactional
    public void updateOrder(Integer id, KioskOrderUpsertDto dto) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(KioskOrder.class, "id", id));

        GarbagePoint gp = garbagePointRepository.findById(dto.getGarbagePointId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        GarbagePoint.class, "id", dto.getGarbagePointId()
                ));

        ContainerSize cs = containerSizeRepository.findById(dto.getContainerSizeId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        ContainerSize.class, "id", dto.getContainerSizeId()
                ));

        Fraction fraction = fractionRepository.findById(dto.getFractionId())
                .orElseThrow(() -> ResourceNotFoundException.of(
                        Fraction.class, "id", dto.getFractionId()
                ));

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> ResourceNotFoundException.of(
                            User.class, "id", dto.getUserId()
                    ));

            if (user.getRole() != UserRole.KIOSK && user.getRole() != UserRole.ADMIN) {
                throw new BadRequestException(
                        "Пользователь id=%d не может оформлять заказ киоска".formatted(dto.getUserId())
                );
            }
        }

        order.setGarbagePoint(gp);
        order.setContainerSize(cs);
        order.setFraction(fraction);
        order.setUser(user);

        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }

        // createdAt не трогаем
        // save() не обязателен, но можно:
        kioskOrderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(KioskOrder.class, "id", id));

        kioskOrderRepository.delete(order);
    }
}
