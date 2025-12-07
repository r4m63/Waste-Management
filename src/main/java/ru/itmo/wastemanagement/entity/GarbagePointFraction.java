package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "garbage_point_fractions")
public class GarbagePointFraction {

    @EmbeddedId
    private GarbagePointFractionId id;

    @MapsId("garbagePointId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garbage_point_id", nullable = false)
    private GarbagePoint garbagePoint;

    @MapsId("fractionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraction_id", nullable = false)
    private Fraction fraction;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

