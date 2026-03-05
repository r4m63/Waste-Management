package ru.itmo.wastemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.vehicle.VehicleUpsertDto;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.VehicleGridRepository;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleGridRepository vehicleGridRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void queryGridReturnsMappedRows() {
        GridTableRequest req = GridTableRequest.builder().startRow(0).endRow(10).build();
        Vehicle v = new Vehicle();
        v.setId(2);
        v.setPlateNumber("A123AA");
        when(vehicleGridRepository.findPageByGrid(req)).thenReturn(List.of(v));
        when(vehicleGridRepository.countByGrid(req)).thenReturn(1L);

        var result = vehicleService.queryGrid(req);

        assertThat(result.getRows()).hasSize(1);
        assertThat(result.getRows().get(0).getPlateNumber()).isEqualTo("A123AA");
        assertThat(result.getLastRow()).isEqualTo(1);
    }

    @Test
    void createVehicleThrowsOnDuplicatePlate() {
        VehicleUpsertDto dto = dto(" A111AA ");
        when(vehicleRepository.existsByPlateNumber("A111AA")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createVehicleTrimsAndReturnsId() {
        VehicleUpsertDto dto = dto(" A222AA ");
        when(vehicleRepository.existsByPlateNumber("A222AA")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(99);
            return v;
        });

        Integer id = vehicleService.createVehicle(dto);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getPlateNumber()).isEqualTo("A222AA");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(id).isEqualTo(99);
    }

    @Test
    void updateVehicleThrowsWhenNotFound() {
        when(vehicleRepository.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(5, dto("X")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateVehicleThrowsOnPlateConflict() {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber("OLD");
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlateNumber("NEW")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle(1, dto(" NEW ")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateVehicleUpdatesFields() {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber("OLD");
        vehicle.setActive(true);
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlateNumber("NEW")).thenReturn(false);

        VehicleUpsertDto dto = dto(" NEW ");
        dto.setName(" Van ");
        dto.setCapacity(123);
        dto.setActive(false);

        vehicleService.updateVehicle(1, dto);

        assertThat(vehicle.getPlateNumber()).isEqualTo("NEW");
        assertThat(vehicle.getName()).isEqualTo("Van");
        assertThat(vehicle.getCapacity()).isEqualTo(123);
        assertThat(vehicle.isActive()).isFalse();
    }

    @Test
    void deleteVehicleDeletesEntity() {
        Vehicle vehicle = new Vehicle();
        when(vehicleRepository.findById(10)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(10);

        verify(vehicleRepository).delete(vehicle);
    }

    private static VehicleUpsertDto dto(String plate) {
        VehicleUpsertDto dto = new VehicleUpsertDto();
        dto.setPlateNumber(plate);
        dto.setCapacity(1);
        dto.setActive(true);
        return dto;
    }
}
