package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.FractionDto;
import ru.itmo.wastemanagement.dto.GarbagePointCreateDto;
import ru.itmo.wastemanagement.dto.GarbagePointDto;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.GarbagePointFraction;
import ru.itmo.wastemanagement.entity.GarbagePointFractionId;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.FractionRepository;
import ru.itmo.wastemanagement.repository.GarbagePointFractionRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarbagePointService {

    private final GarbagePointRepository garbagePointRepository;
    private final GarbagePointFractionRepository garbagePointFractionRepository;
    private final FractionRepository fractionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<GarbagePointDto> findAll() {
        return garbagePointRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GarbagePointDto> findAllOpen() {
        return garbagePointRepository.findByOpenTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GarbagePointDto findById(Integer id) {
        GarbagePoint point = garbagePointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", id));
        return toDto(point);
    }

    @Transactional
    public GarbagePointDto create(GarbagePointCreateDto dto) {
        GarbagePoint point = new GarbagePoint();
        point.setAddress(dto.getAddress());
        point.setCapacity(dto.getCapacity());
        point.setOpen(dto.isOpen());
        point.setLat(dto.getLat());
        point.setLon(dto.getLon());
        point.setCreatedAt(LocalDateTime.now());

        if (dto.getAdminId() != null) {
            User admin = userRepository.findById(dto.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAdminId()));
            point.setAdmin(admin);
        }

        GarbagePoint saved = garbagePointRepository.save(point);

        // Связываем с фракциями
        if (dto.getFractionIds() != null && !dto.getFractionIds().isEmpty()) {
            for (Integer fractionId : dto.getFractionIds()) {
                Fraction fraction = fractionRepository.findById(fractionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Fraction", "id", fractionId));

                GarbagePointFraction gpf = new GarbagePointFraction();
                gpf.setId(new GarbagePointFractionId(saved.getId(), fractionId));
                gpf.setGarbagePoint(saved);
                gpf.setFraction(fraction);
                gpf.setActive(true);
                garbagePointFractionRepository.save(gpf);
            }
        }

        return toDto(saved);
    }

    @Transactional
    public GarbagePointDto update(Integer id, GarbagePointCreateDto dto) {
        GarbagePoint point = garbagePointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GarbagePoint", "id", id));

        point.setAddress(dto.getAddress());
        point.setCapacity(dto.getCapacity());
        point.setOpen(dto.isOpen());
        point.setLat(dto.getLat());
        point.setLon(dto.getLon());

        if (dto.getAdminId() != null) {
            User admin = userRepository.findById(dto.getAdminId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAdminId()));
            point.setAdmin(admin);
        } else {
            point.setAdmin(null);
        }

        GarbagePoint saved = garbagePointRepository.save(point);

        // Обновляем связи с фракциями
        if (dto.getFractionIds() != null) {
            // Удаляем старые связи
            garbagePointFractionRepository.deleteByGarbagePointId(id);

            // Создаем новые связи
            for (Integer fractionId : dto.getFractionIds()) {
                Fraction fraction = fractionRepository.findById(fractionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Fraction", "id", fractionId));

                GarbagePointFraction gpf = new GarbagePointFraction();
                gpf.setId(new GarbagePointFractionId(saved.getId(), fractionId));
                gpf.setGarbagePoint(saved);
                gpf.setFraction(fraction);
                gpf.setActive(true);
                garbagePointFractionRepository.save(gpf);
            }
        }

        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!garbagePointRepository.existsById(id)) {
            throw new ResourceNotFoundException("GarbagePoint", "id", id);
        }
        garbagePointFractionRepository.deleteByGarbagePointId(id);
        garbagePointRepository.deleteById(id);
    }

    private GarbagePointDto toDto(GarbagePoint point) {
        List<GarbagePointFraction> fractions = garbagePointFractionRepository
                .findByGarbagePointIdAndActiveTrue(point.getId());

        List<FractionDto> fractionDtos = fractions.stream()
                .map(gpf -> FractionDto.builder()
                        .id(gpf.getFraction().getId())
                        .name(gpf.getFraction().getName())
                        .code(gpf.getFraction().getCode())
                        .description(gpf.getFraction().getDescription())
                        .hazardous(gpf.getFraction().isHazardous())
                        .build())
                .collect(Collectors.toList());

        return GarbagePointDto.builder()
                .id(point.getId())
                .address(point.getAddress())
                .capacity(point.getCapacity())
                .open(point.isOpen())
                .lat(point.getLat())
                .lon(point.getLon())
                .createdAt(point.getCreatedAt())
                .adminId(point.getAdmin() != null ? point.getAdmin().getId() : null)
                .adminName(point.getAdmin() != null ? point.getAdmin().getName() : null)
                .fractions(fractionDtos)
                .build();
    }
}
