package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.wastemanagement.entity.ContainerSize;

public interface ContainerSizeRepository extends JpaRepository<ContainerSize, Long> {

    boolean existsByCode(String code);
}
