package ru.itmo.wastemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "garbage_points")
public class GarbagePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    private Double lat;

    private Double lon;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiosk_id")
    private User kiosk;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "garbage_point_fractions",
            joinColumns = @JoinColumn(name = "garbage_point_id"),
            inverseJoinColumns = @JoinColumn(name = "fraction_id")
    )
    private Set<Fraction> fractions = new HashSet<>();

    public void addFraction(Fraction fraction) {
        this.fractions.add(fraction);
        fraction.getGarbagePoints().add(this);
    }

    public void removeFraction(Fraction fraction) {
        this.fractions.remove(fraction);
        fraction.getGarbagePoints().remove(this);
    }
}

