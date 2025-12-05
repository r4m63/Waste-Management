package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Incident;
import ru.itmo.wastemanagement.entity.enums.IncidentType;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    List<Incident> findByStopId(Integer stopId);
    List<Incident> findByResolvedFalseOrderByCreatedAtDesc();
    List<Incident> findByType(IncidentType type);
    List<Incident> findByCreatedById(Integer createdById);
}
