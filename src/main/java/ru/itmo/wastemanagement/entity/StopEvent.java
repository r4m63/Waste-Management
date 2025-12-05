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
import ru.itmo.wastemanagement.entity.enums.StopEventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "stop_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private RouteStop stop;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private StopEventType eventType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "photo_url")
    private String photoUrl;

    private String comment;
}

