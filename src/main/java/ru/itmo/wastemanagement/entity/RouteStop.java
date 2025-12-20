package ru.itmo.wastemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "route_stops")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
    private LocalDateTime timeFrom;

    @Column(name = "time_to")
    private LocalDateTime timeTo;

    @Column(name = "expected_capacity")
    private Integer expectedCapacity;

    @Column(name = "actual_capacity")
    private Integer actualCapacity;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "stop_status")
    @Builder.Default
    private StopStatus status = StopStatus.planned;

    private String note;
}
