package ru.itmo.wastemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "route_stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "seq_no", nullable = false)
    private Integer seqNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garbage_point_id")
    private GarbagePoint garbagePoint;

    private String address;

    @Column(name = "time_from")
    private OffsetDateTime timeFrom;

    @Column(name = "time_to")
    private OffsetDateTime timeTo;

    @Column(name = "expected_capacity")
    private Integer expectedCapacity;

    @Column(name = "actual_capacity")
    private Integer actualCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StopStatus status = StopStatus.planned;

    private String note;
}

