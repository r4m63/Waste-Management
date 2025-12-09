package ru.itmo.wastemanagement.dto.gridtable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridTableSortModel {
    private String colId;  // "address", "capacity", "admin.id", ...
    private String sort;   // "asc" | "desc"
}
