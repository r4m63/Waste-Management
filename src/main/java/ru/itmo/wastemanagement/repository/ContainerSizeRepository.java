package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.ContainerSize;

@Repository
public interface ContainerSizeRepository extends JpaRepository<ContainerSize, Integer> {
}

