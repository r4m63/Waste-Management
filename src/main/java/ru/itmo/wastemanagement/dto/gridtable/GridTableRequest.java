package ru.itmo.wastemanagement.dto.gridtable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GridTableRequest {

    @NotNull
    private Integer startRow;

    @NotNull
    private Integer endRow;

    // [{ "colId": "createdAt", "sort": "desc" }, ...]
    private List<GridTableSortModel> sortModel;

    // colId -> filter descriptor
    // см. формат filterModel из ag-Grid
    private Map<String, Object> filterModel;
}

