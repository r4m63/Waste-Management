package ru.itmo.wastemanagement.dto.containersize;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import ru.itmo.wastemanagement.entity.ContainerSize;

@Value
@Builder
public class ContainerSizeRowDto {
    Long id;
    String code;
    Integer capacity;
    Double length;
    Double width;
    Double height;
    String description;
    LocalDateTime createdAt;

    public static ContainerSizeRowDto fromEntity(ContainerSize entity) {
        return ContainerSizeRowDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .capacity(entity.getCapacity())
                .length(entity.getLength())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
