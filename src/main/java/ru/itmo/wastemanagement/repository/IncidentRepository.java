package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Incident;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    
    List<Incident> findByStop_Id(Integer stopId);
    
    List<Incident> findByResolvedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT i FROM Incident i ORDER BY i.createdAt DESC")
    List<Incident> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT i FROM Incident i WHERE i.stop.route.id = :routeId ORDER BY i.createdAt DESC")
    List<Incident> findByRouteId(Integer routeId);
}

