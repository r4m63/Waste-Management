package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.dto.gridtable.GridTableResponse;
import ru.itmo.wastemanagement.dto.vehicle.VehicleRowDto;
import ru.itmo.wastemanagement.dto.vehicle.VehicleUpsertDto;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.VehicleGridRepository;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleGridRepository vehicleGridRepository;

    @Transactional(readOnly = true)
    public GridTableResponse<VehicleRowDto> queryGrid(GridTableRequest req) {
        List<Vehicle> rows = vehicleGridRepository.findPageByGrid(req);
        long total = vehicleGridRepository.countByGrid(req);

        List<VehicleRowDto> dtos = rows.stream()
                .map(VehicleRowDto::fromEntity)
                .toList();

        return GridTableResponse.<VehicleRowDto>builder()
                .rows(dtos)
                .lastRow((int) total)
                .build();
    }

    @Transactional
    public Integer createVehicle(VehicleUpsertDto dto) {
        String plate = dto.getPlateNumber().trim();

        if (vehicleRepository.existsByPlateNumber(plate)) {
            throw new ConflictException("Транспортное средство с таким госномером уже существует");
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(plate)
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .capacity(dto.getCapacity())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .createdAt(LocalDateTime.now())
                .build();

        return vehicleRepository.save(vehicle).getId();
    }

    @Transactional
    public void updateVehicle(Integer id, VehicleUpsertDto dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        String newPlate = dto.getPlateNumber().trim();

        // если номер поменялся — проверяем уникальность
        if (!newPlate.equals(vehicle.getPlateNumber())) {
            if (vehicleRepository.existsByPlateNumber(newPlate)) {
                throw new ConflictException("Транспортное средство с таким госномером уже существует");
            }
            vehicle.setPlateNumber(newPlate);
        }

        vehicle.setName(dto.getName() != null ? dto.getName().trim() : null);
        vehicle.setCapacity(dto.getCapacity());
        vehicle.setActive(dto.getActive() != null ? dto.getActive() : vehicle.isActive());

        // save() не обязателен — JPA сам засинхает
    }

    @Transactional
    public void deleteVehicle(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        vehicleRepository.delete(vehicle);
    }
}
