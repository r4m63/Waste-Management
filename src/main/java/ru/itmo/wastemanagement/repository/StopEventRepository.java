package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.StopEvent;
import ru.itmo.wastemanagement.entity.enums.StopEventType;

import java.util.List;

@Repository
public interface StopEventRepository extends JpaRepository<StopEvent, Integer> {
    List<StopEvent> findByStopIdOrderByCreatedAtAsc(Integer stopId);
    List<StopEvent> findByStopIdAndEventType(Integer stopId, StopEventType eventType);
}
