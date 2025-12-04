package ru.itmo.wastemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;

@Entity
@Table(name = "container_sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

