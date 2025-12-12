package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "container_sizes")
public class ContainerSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private Integer capacity;

    private Double length;
    private Double width;
    private Double height;

    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
