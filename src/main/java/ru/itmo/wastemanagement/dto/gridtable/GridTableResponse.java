package ru.itmo.wastemanagement.dto.gridtable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridTableResponse<T> {
    private List<T> rows;
    private Integer lastRow;
}

