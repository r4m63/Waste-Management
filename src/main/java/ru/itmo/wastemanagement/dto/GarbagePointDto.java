package ru.itmo.wastemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class GarbagePointDto {

    private Integer id;

    @NotBlank
    private String address;

    @NotNull
    @PositiveOrZero
    private Integer capacity;

    @NotNull
    private Boolean open;

    private Double lat;
    private Double lon;

    private LocalDateTime createdAt;

    // айдишники пользователей, с которыми свяжем
    private Integer adminId;
    private Integer kioskId;

    public static GarbagePointDto toDto(GarbagePoint gp) {
        return GarbagePointDto.builder()
                .id(gp.getId())
                .address(gp.getAddress())
                .capacity(gp.getCapacity())
                .open(gp.isOpen())
                .lat(gp.getLat())
                .lon(gp.getLon())
                .createdAt(gp.getCreatedAt())
                .adminId(gp.getAdmin() != null ? gp.getAdmin().getId() : null)
                .kioskId(gp.getKiosk() != null ? gp.getKiosk().getId() : null)
                .build();
    }

    public static GarbagePoint toEntity(GarbagePointDto dto, GarbagePoint targetOrNull) {
        GarbagePoint t = targetOrNull != null ? targetOrNull : new GarbagePoint();
        t.setAddress(dto.getAddress());
        t.setCapacity(dto.getCapacity());
        t.setOpen(dto.getOpen() != null ? dto.getOpen() : true);
        t.setLat(dto.getLat());
        t.setLon(dto.getLon());
        // createdAt и связи admin/kiosk удобнее ставить в сервисе
        return t;
    }
}
