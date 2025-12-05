package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;

import java.util.Optional;

@Repository
public interface ContainerSizeRepository extends JpaRepository<ContainerSize, Integer> {
    Optional<ContainerSize> findByCode(ContainerSizeCode code);
}
