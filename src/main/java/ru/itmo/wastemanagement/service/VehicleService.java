package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.VehicleCreateDto;
import ru.itmo.wastemanagement.dto.VehicleDto;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<VehicleDto> findAll() {
        return vehicleRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> findAllActive() {
        return vehicleRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleDto findById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        return toDto(vehicle);
    }

    @Transactional
    public VehicleDto create(VehicleCreateDto dto) {
        if (vehicleRepository.existsByPlateNumber(dto.getPlateNumber())) {
            throw new ConflictException("Vehicle with plate number '" + dto.getPlateNumber() + "' already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setName(dto.getName());
        vehicle.setCapacity(dto.getCapacity());
        vehicle.setActive(dto.isActive());

        Vehicle saved = vehicleRepository.save(vehicle);
        return toDto(saved);
    }

    @Transactional
    public VehicleDto update(Integer id, VehicleCreateDto dto) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        if (!vehicle.getPlateNumber().equals(dto.getPlateNumber())
                && vehicleRepository.existsByPlateNumber(dto.getPlateNumber())) {
            throw new ConflictException("Vehicle with plate number '" + dto.getPlateNumber() + "' already exists");
        }

        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setName(dto.getName());
        vehicle.setCapacity(dto.getCapacity());
        vehicle.setActive(dto.isActive());

        Vehicle saved = vehicleRepository.save(vehicle);
        return toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", "id", id);
        }
        vehicleRepository.deleteById(id);
    }

    private VehicleDto toDto(Vehicle vehicle) {
        return VehicleDto.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .name(vehicle.getName())
                .capacity(vehicle.getCapacity())
                .active(vehicle.isActive())
                .build();
    }
}

