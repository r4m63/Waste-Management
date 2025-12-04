package ru.itmo.wastemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.entity.Fraction;

@Repository
public interface FractionRepository extends JpaRepository<Fraction, Integer> {
}

