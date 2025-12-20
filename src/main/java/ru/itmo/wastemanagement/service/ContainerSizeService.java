package ru.itmo.wastemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeRowDto;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeUpsertDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.ContainerSizeGridRepository;
import ru.itmo.wastemanagement.repository.ContainerSizeRepository;

@Service
@RequiredArgsConstructor
public class ContainerSizeService {

    private final ContainerSizeRepository containerSizeRepository;
    private final ContainerSizeGridRepository containerSizeGridRepository;

    @Transactional(readOnly = true)
    public GridTableResponse<ContainerSizeRowDto> queryGrid(GridTableRequest req) {
        List<ContainerSize> rows = containerSizeGridRepository.findPageByGrid(req);
        long total = containerSizeGridRepository.countByGrid(req);

        List<ContainerSizeRowDto> dtos = rows.stream()
                .map(ContainerSizeRowDto::fromEntity)
                .toList();

        return GridTableResponse.<ContainerSizeRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Long createContainerSize(ContainerSizeUpsertDto dto) {
        String code = dto.getCode().trim();
        if (containerSizeRepository.existsByCode(code)) {
            throw new ConflictException("Размер контейнера с таким кодом уже существует");
        }

        ContainerSize entity = ContainerSize.builder()
                .code(code)
                .capacity(dto.getCapacity())
                .length(dto.getLength())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        return containerSizeRepository.save(entity).getId();
    }

    @Transactional
    public void updateContainerSize(Long id, ContainerSizeUpsertDto dto) {
        ContainerSize entity = containerSizeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ContainerSize.class, "id", id));

        String newCode = dto.getCode().trim();
        if (!newCode.equals(entity.getCode()) && containerSizeRepository.existsByCode(newCode)) {
            throw new ConflictException("Размер контейнера с таким кодом уже существует");
        }

        entity.setCode(newCode);
        entity.setCapacity(dto.getCapacity());
        entity.setLength(dto.getLength());
        entity.setWidth(dto.getWidth());
        entity.setHeight(dto.getHeight());
        entity.setDescription(dto.getDescription());
    }

    @Transactional
    public void deleteContainerSize(Long id) {
        ContainerSize entity = containerSizeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ContainerSize.class, "id", id));

        containerSizeRepository.delete(entity);
    }
}
