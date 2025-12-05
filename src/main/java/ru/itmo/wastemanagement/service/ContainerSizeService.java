package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.ContainerSizeDto;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.ContainerSizeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContainerSizeService {

    private final ContainerSizeRepository containerSizeRepository;

    @Transactional(readOnly = true)
    public List<ContainerSizeDto> findAll() {
        return containerSizeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContainerSizeDto findById(Integer id) {
        ContainerSize size = containerSizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContainerSize", "id", id));
        return toDto(size);
    }

    @Transactional(readOnly = true)
    public ContainerSizeDto findByCode(ContainerSizeCode code) {
        ContainerSize size = containerSizeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("ContainerSize", "code", code));
        return toDto(size);
    }

    private ContainerSizeDto toDto(ContainerSize size) {
        return ContainerSizeDto.builder()
                .id(size.getId())
                .code(size.getCode())
                .capacity(size.getCapacity())
                .build();
    }
}

