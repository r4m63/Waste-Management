package ru.itmo.wastemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.wastemanagement.dto.shift.DriverShiftDto;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.exception.BadRequestException;
import ru.itmo.wastemanagement.exception.ResourceNotFoundException;
import ru.itmo.wastemanagement.repository.DriverShiftRepository;
import ru.itmo.wastemanagement.repository.UserRepository;
import ru.itmo.wastemanagement.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverShiftService {

    private final DriverShiftRepository driverShiftRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<DriverShiftDto> getAllShifts() {
        return driverShiftRepository.findAllOrderByOpenedAtDesc()
                .stream()
                .map(DriverShiftDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverShiftDto> getShiftsByDriverId(Integer driverId) {
        return driverShiftRepository.findByDriver_IdOrderByOpenedAtDesc(driverId)
                .stream()
                .map(DriverShiftDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<DriverShiftDto> getCurrentShiftByDriverLogin(String driverLogin) {
        if (driverLogin == null || driverLogin.isBlank()) {
            return Optional.empty();
        }

        User driver = userRepository.findByLogin(driverLogin).orElse(null);
        if (driver == null) {
            return Optional.empty();
        }

        return driverShiftRepository.findByDriver_IdAndStatus(driver.getId(), ShiftStatus.open)
                .map(DriverShiftDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public boolean hasOpenShift(String driverLogin) {
        if (driverLogin == null || driverLogin.isBlank()) {
            return false;
        }

        User driver = userRepository.findByLogin(driverLogin).orElse(null);
        if (driver == null) {
            return false;
        }

        return driverShiftRepository.existsByDriver_IdAndStatus(driver.getId(), ShiftStatus.open);
    }

    @Transactional
    public DriverShiftDto openShift(String driverLogin, Integer vehicleId) {
        if (driverLogin == null || driverLogin.isBlank()) {
            throw new BadRequestException("Не указан водитель");
        }

        User driver = userRepository.findByLogin(driverLogin)
                .orElseThrow(() -> new ResourceNotFoundException("User", "login", driverLogin));

        if (driver.getRole() != UserRole.DRIVER) {
            throw new BadRequestException("Пользователь не является водителем");
        }

        // Check if driver already has an open shift
        if (driverShiftRepository.existsByDriver_IdAndStatus(driver.getId(), ShiftStatus.open)) {
            throw new BadRequestException("У водителя уже есть открытая смена");
        }

        Vehicle vehicle = null;
        if (vehicleId != null) {
            vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));
        }

        DriverShift shift = DriverShift.builder()
                .driver(driver)
                .vehicle(vehicle)
                .openedAt(LocalDateTime.now())
                .status(ShiftStatus.open)
                .build();

        shift = driverShiftRepository.save(shift);
        return DriverShiftDto.fromEntity(shift);
    }

    @Transactional
    public DriverShiftDto closeShift(Integer shiftId, String driverLogin) {
        DriverShift shift = driverShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("DriverShift", "id", shiftId));

        // Verify the shift belongs to the driver
        if (driverLogin != null && !driverLogin.isBlank()) {
            User driver = userRepository.findByLogin(driverLogin).orElse(null);
            if (driver != null && shift.getDriver() != null 
                    && !shift.getDriver().getId().equals(driver.getId())) {
                throw new BadRequestException("Смена не принадлежит этому водителю");
            }
        }

        if (shift.getStatus() == ShiftStatus.closed) {
            throw new BadRequestException("Смена уже закрыта");
        }

        shift.setStatus(ShiftStatus.closed);
        shift.setClosedAt(LocalDateTime.now());

        shift = driverShiftRepository.save(shift);
        return DriverShiftDto.fromEntity(shift);
    }
}

