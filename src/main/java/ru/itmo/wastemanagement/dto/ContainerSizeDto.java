package ru.itmo.wastemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.wastemanagement.entity.enums.ContainerSizeCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerSizeDto {
    private Integer id;
    private ContainerSizeCode code;
    private Integer capacity;
}

