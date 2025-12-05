package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Fraction;

import java.util.Optional;

@Repository
public interface FractionRepository extends JpaRepository<Fraction, Integer> {
    Optional<Fraction> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
