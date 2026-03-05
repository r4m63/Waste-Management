package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.garbagepoint.GarbagePointCreateUpdateDto;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.GarbagePointGridRepository;
import ru.itmo.wastemanagement.repository.GarbagePointRepository;
import ru.itmo.wastemanagement.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarbagePointServiceTest {

    @Mock
    private GarbagePointRepository garbagePointRepository;

    @Mock
    private GarbagePointGridRepository gridRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GarbagePointService garbagePointService;

    @Test
    void queryGridReturnsRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        GarbagePoint gp = new GarbagePoint();
        gp.setId(1);
        gp.setAddress("Addr");
        when(gridRepository.findPageByGrid(req)).thenReturn(List.of(gp));
        when(gridRepository.countByGrid(req)).thenReturn(1L);

        var result = garbagePointService.queryGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getAddress()).isEqualTo("Addr");
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createNewGarbagePointWithKioskSavesEntity() {
        GarbagePointCreateUpdateDto dto = dto();
        dto.setKioskId(9);
        User kiosk = new User();
        kiosk.setId(9);
        when(userRepository.findById(9)).thenReturn(Optional.of(kiosk));
        when(garbagePointRepository.save(any(GarbagePoint.class))).thenAnswer(inv -> {
            GarbagePoint gp = inv.getArgument(0);
            gp.setId(100);
            return gp;
        });

        Integer id = garbagePointService.createNewGarbagePoint(dto);

        ArgumentCaptor<GarbagePoint> captor = ArgumentCaptor.forClass(GarbagePoint.class);
        verify(garbagePointRepository).save(captor.capture());
        GarbagePoint saved = captor.getValue();
        assertThat(saved.getAddress()).isEqualTo("A");
        assertThat(saved.getKiosk()).isEqualTo(kiosk);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(id).isEqualTo(100);
    }

    @Test
    void createNewGarbagePointThrowsWhenKioskNotFound() {
        GarbagePointCreateUpdateDto dto = dto();
        dto.setKioskId(10);
        when(userRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> garbagePointService.createNewGarbagePoint(dto))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateThrowsWhenPointNotFound() {
        when(garbagePointRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> garbagePointService.update(1, dto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateClearsKioskWhenIdNullAndSaves() {
        GarbagePoint gp = new GarbagePoint();
        gp.setKiosk(new User());
        when(garbagePointRepository.findById(1)).thenReturn(Optional.of(gp));

        GarbagePointCreateUpdateDto dto = dto();
        dto.setAddress("  New ");
        dto.setOpen(null);
        dto.setKioskId(null);

        garbagePointService.update(1, dto);

        assertThat(gp.getAddress()).isEqualTo("New");
        assertThat(gp.isOpen()).isTrue();
        assertThat(gp.getKiosk()).isNull();
        verify(garbagePointRepository).save(gp);
    }

    @Test
    void deleteRemovesEntity() {
        GarbagePoint gp = new GarbagePoint();
        when(garbagePointRepository.findById(5)).thenReturn(Optional.of(gp));

        garbagePointService.delete(5);

        verify(garbagePointRepository).delete(gp);
    }

    @Test
    void getOpenPointsReturnsMappedList() {
        GarbagePoint gp = new GarbagePoint();
        gp.setId(2);
        gp.setAddress("Open");
        when(garbagePointRepository.findByOpenTrue()).thenReturn(List.of(gp));

        var result = garbagePointService.getOpenPoints();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    private static GarbagePointCreateUpdateDto dto() {
        GarbagePointCreateUpdateDto dto = new GarbagePointCreateUpdateDto();
        dto.setAddress("A");
        dto.setCapacity(100);
        dto.setOpen(true);
        dto.setLat(59.0);
        dto.setLon(30.0);
        return dto;
    }
}
