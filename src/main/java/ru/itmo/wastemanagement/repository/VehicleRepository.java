package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.wastemanagement.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    boolean existsByPlateNumber(String plateNumber);
}
