package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.DriverShift;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverShiftRepository extends JpaRepository<DriverShift, Integer> {
    
    List<DriverShift> findByDriver_IdOrderByOpenedAtDesc(Integer driverId);
    
    Optional<DriverShift> findByDriver_IdAndStatus(Integer driverId, ShiftStatus status);
    
    @Query("SELECT s FROM DriverShift s ORDER BY s.openedAt DESC")
    List<DriverShift> findAllOrderByOpenedAtDesc();
    
    @Query("SELECT s FROM DriverShift s WHERE s.status = 'open' ORDER BY s.openedAt DESC")
    List<DriverShift> findAllOpenShifts();
    
    boolean existsByDriver_IdAndStatus(Integer driverId, ShiftStatus status);
}

