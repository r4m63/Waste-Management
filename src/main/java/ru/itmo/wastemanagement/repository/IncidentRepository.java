package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Incident;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
}

