// src/components/tableData/FractionsTable.jsx

import React, { useCallback, useMemo, useRef } from "react";
import { AgGridTable } from "@/components/tableData/AgGridTable.jsx";
import { API_BASE } from "../../../cfg.js";
import { Button } from "@/components/ui/button";
import { MoreHorizontal } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function FractionsTable({
                                           onOpenEditFractionModal,
                                           onDeleteFraction,
                                           onReadyRefresh,
                                       }) {
    const gridApiRef = useRef(null);

    const columnDefs = useMemo(
        () => [
            {
                headerName: "ID",
                field: "id",
                colId: "id",
                width: 80,
                sortable: true,
                filter: "agNumberColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Действия",
                colId: "actions",
                width: 90,
                sortable: false,
                filter: false,
                floatingFilter: false,
                cellRenderer: (p) => (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <span className="sr-only">Открыть меню</span>
                                <MoreHorizontal className="h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem
                                onClick={() => onOpenEditFractionModal?.(p.data)}
                            >
                                Редактировать
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => onDeleteFraction?.(p.data)}
                            >
                                Удалить
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                ),
            },
            {
                headerName: "Название",
                field: "name",
                colId: "name",
                flex: 1.5,
                sortable: true,
                filter: "agTextColumnFilter", // Используем стандартный текстовый фильтр
                floatingFilter: true,
            },
            {
                headerName: "Код",
                field: "code",
                colId: "code",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter", // Используем стандартный текстовый фильтр
                floatingFilter: true,
            },
            {
                headerName: "Описание",
                field: "description",
                colId: "description",
                flex: 2,
                sortable: true,
                filter: "agTextColumnFilter", // Используем стандартный текстовый фильтр
                floatingFilter: true,
            },
            {
                headerName: "Опасная",
                field: "hazardous",
                colId: "hazardous",
                width: 120,
                sortable: true,
                filter: "agTextColumnFilter", // Заменили agSetColumnFilter на текстовый фильтр
                floatingFilter: true,
                valueFormatter: ({ value }) =>
                    value === true ? "Да" : value === false ? "Нет" : "",
            },
        ],
        [onOpenEditFractionModal, onDeleteFraction]
    );

    const mapSortModel = (sm = []) =>
        sm.map((s) => ({ colId: s.colId, sort: s.sort }));

    const makeDatasource = useCallback(
        () => ({
            getRows: async (params) => {
                try {
                    const body = {
                        startRow: params.startRow,
                        endRow: params.endRow,
                        sortModel: mapSortModel(params.sortModel),
                        filterModel: params.filterModel || {},
                    };

                    const res = await fetch(`${API_BASE}/api/fractions/query`, {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                        },
                        body: JSON.stringify(body),
                    });

                    if (!res.ok) {
                        params.failCallback();
                        return;
                    }

                    const data = await res.json();
                    params.successCallback(data.rows || [], data.lastRow ?? 0);
                } catch (e) {
                    console.error(e);
                    params.failCallback();
                }
            },
        }),
        []
    );

    const setDatasource = useCallback(() => {
        if (!gridApiRef.current) return;
        const ds = makeDatasource();
        gridApiRef.current.setGridOption("datasource", ds);
        gridApiRef.current.purgeInfiniteCache();
    }, [makeDatasource]);

    const exposeRefresh = useCallback(() => {
        onReadyRefresh?.(() => {
            if (!gridApiRef.current) return;
            gridApiRef.current.refreshInfiniteCache();
        });
    }, [onReadyRefresh]);

    const onGridReady = useCallback(
        (e) => {
            gridApiRef.current = e.api;
            setDatasource();
            exposeRefresh();
        },
        [setDatasource, exposeRefresh]
    );

    return (
        <AgGridTable
            columnDefs={columnDefs}
            defaultColDef={{
                filter: true,
                sortable: true,
                floatingFilter: true,
                resizable: true,
                flex: 1,
                minWidth: 140,
            }}
            gridOptions={{
                rowModelType: "infinite",
                cacheBlockSize: 50,
                maxBlocksInCache: 2,
                pagination: true,
                paginationPageSize: 50,
                suppressMultiSort: false,
                onGridReady,
            }}
            height="500px"
        />
    );
}
