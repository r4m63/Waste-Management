package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;

import java.util.List;

@Repository
public interface KioskOrderRepository extends JpaRepository<KioskOrder, Integer> {

    @Query("""
            select ko.garbagePoint.id as garbagePointId,
                   coalesce(sum(coalesce(ko.weight, 0)), 0) as totalWeight
            from KioskOrder ko
            where (ko.status is null or ko.status <> ru.itmo.wastemanagement.entity.enums.OrderStatus.CANCELLED)
            group by ko.garbagePoint.id
            """)
    List<Object[]> findActiveWeightsByGarbagePoint();
}
