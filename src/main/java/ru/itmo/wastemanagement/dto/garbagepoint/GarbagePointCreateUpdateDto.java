package ru.itmo.wastemanagement.dto.garbagepoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.GarbagePoint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarbagePointCreateUpdateDto {

    @NotBlank
    private String address;
    @NotNull
    @PositiveOrZero
    private Integer capacity;
    @NotNull
    private Boolean open;
    private Double lat;
    private Double lon;
    private Integer kioskId;

    public static GarbagePointCreateUpdateDto toDto(GarbagePoint gp) {
        if (gp == null) return null;
        return GarbagePointCreateUpdateDto.builder()
                .address(gp.getAddress())
                .capacity(gp.getCapacity())
                .open(gp.isOpen())
                .lat(gp.getLat())
                .lon(gp.getLon())
                .kioskId(gp.getKiosk() != null ? gp.getKiosk().getId() : null)
                .build();
    }

    public static GarbagePoint toEntity(GarbagePointCreateUpdateDto dto, GarbagePoint targetOrNull) {
        GarbagePoint t = targetOrNull != null ? targetOrNull : new GarbagePoint();
        t.setAddress(dto.getAddress());
        t.setCapacity(dto.getCapacity());
        t.setOpen(dto.getOpen() != null ? dto.getOpen() : true);
        t.setLat(dto.getLat());
        t.setLon(dto.getLon());
        return t;
    }

}
