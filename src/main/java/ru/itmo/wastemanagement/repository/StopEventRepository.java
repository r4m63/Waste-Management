package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.StopEvent;

@Repository
public interface StopEventRepository extends JpaRepository<StopEvent, Integer> {
}

