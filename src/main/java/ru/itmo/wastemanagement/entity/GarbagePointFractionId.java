package ru.itmo.wastemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GarbagePointFractionId implements Serializable {

    @Column(name = "garbage_point_id")
    private Integer garbagePointId;

    @Column(name = "fraction_id")
    private Integer fractionId;
}

