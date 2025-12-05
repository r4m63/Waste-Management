package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopDto {
    private Integer id;
    private Integer routeId;
    private Integer seqNo;
    private Integer garbagePointId;
    private String garbagePointAddress;
    private Double garbagePointLat;
    private Double garbagePointLon;
    private String address;
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;
    private Integer expectedCapacity;
    private Integer actualCapacity;
    private StopStatus status;
    private String note;
    private List<StopEventDto> events;
}

