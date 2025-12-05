package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverShiftRepository extends JpaRepository<DriverShift, Integer> {
    List<DriverShift> findByStatus(ShiftStatus status);
    Optional<DriverShift> findByDriverIdAndStatus(Integer driverId, ShiftStatus status);
    List<DriverShift> findByDriverId(Integer driverId);
}
