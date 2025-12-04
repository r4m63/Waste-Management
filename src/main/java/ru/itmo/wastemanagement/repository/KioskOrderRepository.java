package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.KioskOrder;

@Repository
public interface KioskOrderRepository extends JpaRepository<KioskOrder, Integer> {
}

