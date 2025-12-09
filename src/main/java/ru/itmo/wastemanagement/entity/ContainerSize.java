package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;

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
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ContainerSizeCode code;

    @Column(nullable = false)
    private Integer capacity;
}

