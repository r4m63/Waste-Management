package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Route;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
    List<Route> findByDriver_Id(Integer driverId);
}
