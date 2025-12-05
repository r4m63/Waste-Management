package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.FractionCreateDto;
import ru.itmo.wastemanagement.dto.FractionDto;
import ru.itmo.wastemanagement.entity.Fraction;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.FractionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FractionService {

    private final FractionRepository fractionRepository;

    @Transactional(readOnly = true)
    public List<FractionDto> findAll() {
        return fractionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FractionDto findById(Integer id) {
        Fraction fraction = fractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraction", "id", id));
        return toDto(fraction);
    }

    @Transactional(readOnly = true)
    public FractionDto findByCode(String code) {
        Fraction fraction = fractionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Fraction", "code", code));
        return toDto(fraction);
    }

    @Transactional
    public FractionDto create(FractionCreateDto dto) {
        if (fractionRepository.existsByCode(dto.getCode())) {
            throw new ConflictException("Fraction with code '" + dto.getCode() + "' already exists");
        }
        if (fractionRepository.existsByName(dto.getName())) {
            throw new ConflictException("Fraction with name '" + dto.getName() + "' already exists");
        }

        Fraction fraction = new Fraction();
        fraction.setName(dto.getName());
        fraction.setCode(dto.getCode());
        fraction.setDescription(dto.getDescription());
        fraction.setHazardous(dto.isHazardous());
        fraction.setCreatedAt(LocalDateTime.now());
        fraction.setUpdatedAt(LocalDateTime.now());

        Fraction saved = fractionRepository.save(fraction);
        return toDto(saved);
    }

    @Transactional
    public FractionDto update(Integer id, FractionCreateDto dto) {
        Fraction fraction = fractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraction", "id", id));

        // Проверяем уникальность code и name если они изменились
        if (!fraction.getCode().equals(dto.getCode()) && fractionRepository.existsByCode(dto.getCode())) {
            throw new ConflictException("Fraction with code '" + dto.getCode() + "' already exists");
        }
        if (!fraction.getName().equals(dto.getName()) && fractionRepository.existsByName(dto.getName())) {
            throw new ConflictException("Fraction with name '" + dto.getName() + "' already exists");
        }

        fraction.setName(dto.getName());
        fraction.setCode(dto.getCode());
        fraction.setDescription(dto.getDescription());
        fraction.setHazardous(dto.isHazardous());
        fraction.setUpdatedAt(LocalDateTime.now());

        Fraction saved = fractionRepository.save(fraction);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!fractionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fraction", "id", id);
        }
        fractionRepository.deleteById(id);
    }

    private FractionDto toDto(Fraction fraction) {
        return FractionDto.builder()
                .id(fraction.getId())
                .name(fraction.getName())
                .code(fraction.getCode())
                .description(fraction.getDescription())
                .hazardous(fraction.isHazardous())
                .createdAt(fraction.getCreatedAt())
                .updatedAt(fraction.getUpdatedAt())
                .build();
    }
}

