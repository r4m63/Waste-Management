package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.KioskOrderCreateDto;
import ru.itmo.wastemanagement.dto.KioskOrderDto;
import ru.itmo.wastemanagement.entity.*;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KioskOrderService {

    private final KioskOrderRepository kioskOrderRepository;
    private final GarbagePointRepository garbagePointRepository;
    private final GarbagePointFractionRepository garbagePointFractionRepository;
    private final ContainerSizeRepository containerSizeRepository;
    private final FractionRepository fractionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<KioskOrderDto> findAll() {
        return kioskOrderRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KioskOrderDto> findByGarbagePoint(Integer garbagePointId) {
        return kioskOrderRepository.findByGarbagePointId(garbagePointId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KioskOrderDto> findByUser(Integer userId) {
        return kioskOrderRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KioskOrderDto> findByStatus(OrderStatus status) {
        return kioskOrderRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KioskOrderDto findById(Integer id) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KioskOrder", "id", id));
        return toDto(order);
    }

    @Transactional
    public KioskOrderDto create(KioskOrderCreateDto dto) {
        // Валидация точки сбора
        GarbagePoint garbagePoint = garbagePointRepository.findById(dto.getGarbagePointId())
                .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", dto.getGarbagePointId()));

        if (!garbagePoint.isOpen()) {
            throw new BadRequestException("Garbage point is closed");
        }

        // Валидация фракции для точки
        Fraction fraction = fractionRepository.findById(dto.getFractionId())
                .orElseThrow(() -> new ResourceNotFoundException("Fraction", "id", dto.getFractionId()));

        boolean fractionAccepted = garbagePointFractionRepository
                .existsByGarbagePointIdAndFractionIdAndActiveTrue(dto.getGarbagePointId(), dto.getFractionId());
        if (!fractionAccepted) {
            throw new BadRequestException("Fraction '" + fraction.getName() + "' is not accepted at this point");
        }

        // Валидация размера контейнера
        ContainerSize containerSize = containerSizeRepository.findById(dto.getContainerSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("ContainerSize", "id", dto.getContainerSizeId()));

        // Поиск или создание пользователя
        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));
        } else if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            Optional<User> existingUser = userRepository.findByPhone(dto.getPhone());
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                // Создаем нового пользователя
                User newUser = new User();
                newUser.setPhone(dto.getPhone());
                newUser.setRole(UserRole.resident);
                newUser.setActive(true);
                newUser.setCreatedAt(LocalDateTime.now());
                user = userRepository.save(newUser);
            }
        }

        // Создание заказа
        KioskOrder order = new KioskOrder();
        order.setGarbagePoint(garbagePoint);
        order.setContainerSize(containerSize);
        order.setFraction(fraction);
        order.setUser(user);
        order.setStatus(OrderStatus.confirmed);
        order.setCreatedAt(LocalDateTime.now());

        KioskOrder saved = kioskOrderRepository.save(order);
        return toDto(saved);
    }

    @Transactional
    public KioskOrderDto updateStatus(Integer id, OrderStatus status) {
        KioskOrder order = kioskOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KioskOrder", "id", id));

        order.setStatus(status);
        KioskOrder saved = kioskOrderRepository.save(order);
        return toDto(saved);
    }

    @Transactional
    public KioskOrderDto cancel(Integer id) {
        return updateStatus(id, OrderStatus.cancelled);
    }

    @Transactional
    public void delete(Integer id) {
        if (!kioskOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("KioskOrder", "id", id);
        }
        kioskOrderRepository.deleteById(id);
    }

    private KioskOrderDto toDto(KioskOrder order) {
        return KioskOrderDto.builder()
                .id(order.getId())
                .garbagePointId(order.getGarbagePoint().getId())
                .garbagePointAddress(order.getGarbagePoint().getAddress())
                .containerSizeId(order.getContainerSize().getId())
                .containerSizeCode(order.getContainerSize().getCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .userPhone(order.getUser() != null ? order.getUser().getPhone() : null)
                .fractionId(order.getFraction().getId())
                .fractionName(order.getFraction().getName())
                .fractionCode(order.getFraction().getCode())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .build();
    }
}

