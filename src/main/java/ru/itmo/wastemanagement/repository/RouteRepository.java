package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
}

