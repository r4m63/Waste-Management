package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.GarbagePointFraction;
import ru.itmo.wastemanagement.entity.GarbagePointFractionId;

import java.util.List;

@Repository
public interface GarbagePointFractionRepository extends JpaRepository<GarbagePointFraction, GarbagePointFractionId> {
    
    List<GarbagePointFraction> findByGarbagePointIdAndActiveTrue(Integer garbagePointId);
    
    boolean existsByGarbagePointIdAndFractionIdAndActiveTrue(Integer garbagePointId, Integer fractionId);
    
    @Modifying
    @Query("DELETE FROM GarbagePointFraction gpf WHERE gpf.garbagePoint.id = :garbagePointId")
    void deleteByGarbagePointId(@Param("garbagePointId") Integer garbagePointId);
}
