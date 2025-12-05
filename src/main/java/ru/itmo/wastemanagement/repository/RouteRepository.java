package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Route;
import ru.itmo.wastemanagement.entity.enums.RouteStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
    List<Route> findByStatus(RouteStatus status);
    List<Route> findByPlannedDate(LocalDateTime date);
    List<Route> findByDriverId(Integer driverId);
    List<Route> findByDriverIdAndStatus(Integer driverId, RouteStatus status);
    List<Route> findByPlannedDateAndStatus(LocalDateTime date, RouteStatus status);
}
