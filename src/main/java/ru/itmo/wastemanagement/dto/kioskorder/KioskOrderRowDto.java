package ru.itmo.wastemanagement.dto.kioskorder;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;

import java.time.LocalDateTime;

@Value
@Builder
public class KioskOrderRowDto {

    Integer id;

    Integer garbagePointId;
    String garbagePointAddress;

    Long containerSizeId;
    String containerSizeCode;

    Integer fractionId;
    String fractionName;

    Integer userId;
    String userName;

    LocalDateTime createdAt;
    OrderStatus status;

    public static KioskOrderRowDto fromEntity(KioskOrder o) {
        if (o == null) return null;

        return KioskOrderRowDto.builder()
                .id(o.getId())
                .garbagePointId(o.getGarbagePoint() != null ? o.getGarbagePoint().getId() : null)
                .garbagePointAddress(
                        o.getGarbagePoint() != null ? o.getGarbagePoint().getAddress() : null
                )
                .containerSizeId(
                        o.getContainerSize() != null ? o.getContainerSize().getId() : null
                )
                .containerSizeCode(
                        o.getContainerSize() != null ? String.valueOf(o.getContainerSize().getCode()) : null
                )
                .fractionId(
                        o.getFraction() != null ? o.getFraction().getId() : null
                )
                .fractionName(
                        o.getFraction() != null ? o.getFraction().getName() : null
                )
                .userId(
                        o.getUser() != null ? o.getUser().getId() : null
                )
                .userName(
                        o.getUser() != null ? o.getUser().getName() : null
                )
                .createdAt(o.getCreatedAt())
                .status(o.getStatus())
                .build();
    }
}
