package ru.itmo.wastemanagement.dto.route;

import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.RouteStop;
import ru.itmo.wastemanagement.entity.enums.StopStatus;

@Value
@Builder
public class RouteStopDto {
    Integer id;
    Integer seqNo;
    Integer garbagePointId;
    String address;
    StopStatus status;
    Integer expectedCapacity;
    Integer actualCapacity;
    String note;

    public static RouteStopDto fromEntity(RouteStop stop) {
        return RouteStopDto.builder()
                .id(stop.getId())
                .seqNo(stop.getSeqNo())
                .garbagePointId(stop.getGarbagePoint() != null ? stop.getGarbagePoint().getId() : null)
                .address(stop.getAddress())
                .status(stop.getStatus())
                .expectedCapacity(stop.getExpectedCapacity())
                .actualCapacity(stop.getActualCapacity())
                .note(stop.getNote())
                .build();
    }
}
