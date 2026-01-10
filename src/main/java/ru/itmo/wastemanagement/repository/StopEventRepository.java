package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.StopEvent;

import java.util.List;

@Repository
public interface StopEventRepository extends JpaRepository<StopEvent, Integer> {
    
    List<StopEvent> findByStop_IdOrderByCreatedAtDesc(Integer stopId);
    
    @Query("SELECT e FROM StopEvent e WHERE e.stop.route.id = :routeId ORDER BY e.createdAt DESC")
    List<StopEvent> findByRouteId(Integer routeId);
    
    @Query("SELECT e FROM StopEvent e ORDER BY e.createdAt DESC")
    List<StopEvent> findAllOrderByCreatedAtDesc();
}

