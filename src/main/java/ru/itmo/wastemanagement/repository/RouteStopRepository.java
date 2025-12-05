package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.RouteStop;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
}

