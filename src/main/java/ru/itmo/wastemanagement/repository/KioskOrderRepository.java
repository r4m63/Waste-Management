package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;

import java.util.List;

@Repository
public interface KioskOrderRepository extends JpaRepository<KioskOrder, Integer> {
    List<KioskOrder> findByGarbagePointId(Integer garbagePointId);
    List<KioskOrder> findByUserId(Integer userId);
    List<KioskOrder> findByStatus(OrderStatus status);
    List<KioskOrder> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
