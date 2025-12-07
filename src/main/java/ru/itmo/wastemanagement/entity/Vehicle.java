package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plate_number", nullable = false, unique = true)
    private String plateNumber;

    private String name;

    private Integer capacity;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

