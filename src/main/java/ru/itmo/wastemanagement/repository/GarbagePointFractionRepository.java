package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.GarbagePointFraction;
import ru.itmo.wastemanagement.entity.GarbagePointFractionId;

@Repository
public interface GarbagePointFractionRepository extends JpaRepository<GarbagePointFraction, GarbagePointFractionId> {
}

