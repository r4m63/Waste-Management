package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.wastemanagement.entity.enums.ShiftStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // driver_id BIGINT NOT NULL REFERENCES users(id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    // vehicle_id BIGINT REFERENCES vehicles(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "shift_status" // тип PostgreSQL enum
    )
    private ShiftStatus status = ShiftStatus.open;
}
