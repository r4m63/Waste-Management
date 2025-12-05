package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
    List<RouteStop> findByRouteIdOrderBySeqNoAsc(Integer routeId);
    List<RouteStop> findByRouteIdAndStatus(Integer routeId, StopStatus status);
    
    @Query("SELECT MAX(rs.seqNo) FROM RouteStop rs WHERE rs.route.id = :routeId")
    Integer findMaxSeqNoByRouteId(@Param("routeId") Integer routeId);
}
