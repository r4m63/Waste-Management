package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;
import ru.itmo.wastemanagement.entity.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskOrderDto {
    private Integer id;
    private Integer garbagePointId;
    private String garbagePointAddress;
    private Integer containerSizeId;
    private ContainerSizeCode containerSizeCode;
    private Integer userId;
    private String userName;
    private String userPhone;
    private Integer fractionId;
    private String fractionName;
    private String fractionCode;
    private LocalDateTime createdAt;
    private OrderStatus status;
}

