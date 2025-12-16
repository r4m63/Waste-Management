package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.RouteStop;

import java.util.Collection;
import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
    List<RouteStop> findByRoute_IdInOrderByRoute_IdAscSeqNoAsc(Collection<Integer> routeIds);
}
