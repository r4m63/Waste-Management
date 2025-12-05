package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.DriverShiftCreateDto;
import ru.itmo.wastemanagement.dto.DriverShiftDto;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ConflictException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.DriverShiftRepository;
import ru.itmo.wastemanagement.repository.UserRepository;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverShiftService {

    private final DriverShiftRepository driverShiftRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<DriverShiftDto> findAll() {
        return driverShiftRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DriverShiftDto> findByStatus(ShiftStatus status) {
        return driverShiftRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DriverShiftDto> findOpenShifts() {
        return findByStatus(ShiftStatus.open);
    }

    @Transactional(readOnly = true)
    public DriverShiftDto findById(Integer id) {
        DriverShift shift = driverShiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DriverShift", "id", id));
        return toDto(shift);
    }

    @Transactional(readOnly = true)
    public Optional<DriverShiftDto> findOpenShiftByDriver(Integer driverId) {
        return driverShiftRepository.findByDriverIdAndStatus(driverId, ShiftStatus.open)
                .map(this::toDto);
    }

    @Transactional
    public DriverShiftDto openShift(DriverShiftCreateDto dto) {
        User driver = userRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getDriverId()));

        if (driver.getRole() != UserRole.courier) {
            throw new BadRequestException("User must be a courier to open shift");
        }

        // Проверяем, что у водителя нет открытой смены
        Optional<DriverShift> existingOpenShift = driverShiftRepository
                .findByDriverIdAndStatus(dto.getDriverId(), ShiftStatus.open);
        if (existingOpenShift.isPresent()) {
            throw new ConflictException("Driver already has an open shift: " + existingOpenShift.get().getId());
        }

        DriverShift shift = new DriverShift();
        shift.setDriver(driver);
        shift.setOpenedAt(LocalDateTime.now());
        shift.setStatus(ShiftStatus.open);

        if (dto.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId()));
            shift.setVehicle(vehicle);
        }

        DriverShift saved = driverShiftRepository.save(shift);
        return toDto(saved);
    }

    @Transactional
    public DriverShiftDto closeShift(Integer driverId) {
        DriverShift shift = driverShiftRepository.findByDriverIdAndStatus(driverId, ShiftStatus.open)
                .orElseThrow(() -> new ResourceNotFoundException("No open shift found for driver with id", "driverId", driverId));

        shift.setStatus(ShiftStatus.closed);
        shift.setClosedAt(LocalDateTime.now());

        DriverShift saved = driverShiftRepository.save(shift);
        return toDto(saved);
    }

    @Transactional
    public DriverShiftDto closeShiftById(Integer shiftId) {
        DriverShift shift = driverShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverShift", "id", shiftId));

        if (shift.getStatus() == ShiftStatus.closed) {
            throw new BadRequestException("Shift is already closed");
        }

        shift.setStatus(ShiftStatus.closed);
        shift.setClosedAt(LocalDateTime.now());

        DriverShift saved = driverShiftRepository.save(shift);
        return toDto(saved);
    }

    private DriverShiftDto toDto(DriverShift shift) {
        return DriverShiftDto.builder()
                .id(shift.getId())
                .driverId(shift.getDriver().getId())
                .driverName(shift.getDriver().getName())
                .vehicleId(shift.getVehicle() != null ? shift.getVehicle().getId() : null)
                .vehicleName(shift.getVehicle() != null ? shift.getVehicle().getName() : null)
                .vehiclePlateNumber(shift.getVehicle() != null ? shift.getVehicle().getPlateNumber() : null)
                .openedAt(shift.getOpenedAt())
                .closedAt(shift.getClosedAt())
                .status(shift.getStatus())
                .build();
    }
}

