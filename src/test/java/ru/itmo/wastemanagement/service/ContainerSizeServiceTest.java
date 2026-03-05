package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.containersize.ContainerSizeUpsertDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.ContainerSizeGridRepository;
import ru.itmo.wastemanagement.repository.ContainerSizeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContainerSizeServiceTest {

    @Mock
    private ContainerSizeRepository containerSizeRepository;

    @Mock
    private ContainerSizeGridRepository containerSizeGridRepository;

    @InjectMocks
    private ContainerSizeService containerSizeService;

    @Test
    void queryGridReturnsMappedRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        ContainerSize cs = new ContainerSize();
        cs.setId(11L);
        cs.setCode("M");
        cs.setCapacity(120);
        when(containerSizeGridRepository.findPageByGrid(req)).thenReturn(List.of(cs));
        when(containerSizeGridRepository.countByGrid(req)).thenReturn(1L);

        var result = containerSizeService.queryGrid(req);

        assertThat(result.getLastRow()).isEqualTo(1);
        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getId()).isEqualTo(11L);
        assertThat(result.getRows().get(0).getCode()).isEqualTo("M");
    }

    @Test
    void createContainerSizeThrowsOnDuplicateCode() {
        ContainerSizeUpsertDto dto = dto(" M1 ");
        when(containerSizeRepository.existsByCode("M1")).thenReturn(true);

        assertThatThrownBy(() -> containerSizeService.createContainerSize(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void createContainerSizeSavesTrimmedCode() {
        ContainerSizeUpsertDto dto = dto(" M2 ");
        when(containerSizeRepository.existsByCode("M2")).thenReturn(false);
        when(containerSizeRepository.save(any(ContainerSize.class))).thenAnswer(inv -> {
            ContainerSize cs = inv.getArgument(0);
            cs.setId(77L);
            return cs;
        });

        Long id = containerSizeService.createContainerSize(dto);

        ArgumentCaptor<ContainerSize> captor = ArgumentCaptor.forClass(ContainerSize.class);
        verify(containerSizeRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("M2");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(id).isEqualTo(77L);
    }

    @Test
    void updateContainerSizeThrowsWhenNotFound() {
        ContainerSizeUpsertDto dto = dto("A");
        when(containerSizeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> containerSizeService.updateContainerSize(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateContainerSizeThrowsOnDuplicateCode() {
        ContainerSize entity = new ContainerSize();
        entity.setId(1L);
        entity.setCode("OLD");
        when(containerSizeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(containerSizeRepository.existsByCode("NEW")).thenReturn(true);

        ContainerSizeUpsertDto dto = dto(" NEW ");

        assertThatThrownBy(() -> containerSizeService.updateContainerSize(1L, dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateContainerSizeUpdatesFields() {
        ContainerSize entity = new ContainerSize();
        entity.setId(1L);
        entity.setCode("OLD");
        when(containerSizeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(containerSizeRepository.existsByCode("NEW")).thenReturn(false);

        ContainerSizeUpsertDto dto = dto(" NEW ");
        dto.setCapacity(240);
        dto.setLength(10d);
        dto.setWidth(11d);
        dto.setHeight(12d);
        dto.setDescription("d");

        containerSizeService.updateContainerSize(1L, dto);

        assertThat(entity.getCode()).isEqualTo("NEW");
        assertThat(entity.getCapacity()).isEqualTo(240);
        assertThat(entity.getLength()).isEqualTo(10d);
        assertThat(entity.getWidth()).isEqualTo(11d);
        assertThat(entity.getHeight()).isEqualTo(12d);
        assertThat(entity.getDescription()).isEqualTo("d");
    }

    @Test
    void deleteContainerSizeDeletesEntity() {
        ContainerSize entity = new ContainerSize();
        when(containerSizeRepository.findById(2L)).thenReturn(Optional.of(entity));

        containerSizeService.deleteContainerSize(2L);

        verify(containerSizeRepository).delete(entity);
    }

    private static ContainerSizeUpsertDto dto(String code) {
        ContainerSizeUpsertDto dto = new ContainerSizeUpsertDto();
        dto.setCode(code);
        dto.setCapacity(100);
        return dto;
    }
}
