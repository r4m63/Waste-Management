package ru.itmo.wastemanagement.dto.garbagepoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.GarbagePoint;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarbagePointRowDto {

    private Long id;
    private String address;
    private Integer capacity;
    private Boolean open;
    private Double lat;
    private Double lon;
    private LocalDateTime createdAt;
    private Long adminId;
    private Long kioskId;

    public static GarbagePointRowDto fromEntity(GarbagePoint gp) {
        if (gp == null) return null;

        return GarbagePointRowDto.builder()
                .id(gp.getId() != null ? gp.getId().longValue() : null)
                .address(gp.getAddress())
                .capacity(gp.getCapacity())
                .open(gp.isOpen())
                .lat(gp.getLat())
                .lon(gp.getLon())
                .createdAt(gp.getCreatedAt())
                .adminId(gp.getAdmin() != null ? gp.getAdmin().getId().longValue() : null)
                .kioskId(gp.getKiosk() != null ? gp.getKiosk().getId().longValue() : null)
                .build();
    }
}
