// src/components/tableData/ContainerSizesTable.jsx

import React, {useCallback, useMemo, useRef} from "react";
import {AgGridTable} from "@/components/tableData/AgGridTable.jsx";
import {API_BASE} from "../../../cfg.js";
import {Button} from "@/components/ui/button";
import {MoreHorizontal} from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {apiFetch} from "@/lib/apiClient.js";
import {parseApiError} from "@/lib/utils.js";
import {toast} from "sonner";

export default function ContainerSizesTable({
                                               onOpenEditSizeModal,
                                               onDeleteSize,
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
                                <MoreHorizontal className="h-4 w-4"/>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem
                                onClick={() => onOpenEditSizeModal?.(p.data)}
                            >
                                Редактировать
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => onDeleteSize?.(p.data)}
                            >
                                Удалить
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                ),
            },
            {
                headerName: "Код",
                field: "code",
                colId: "code",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Вместимость",
                field: "capacity",
                colId: "capacity",
                width: 140,
                sortable: true,
                filter: "agNumberColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Длина",
                field: "length",
                colId: "length",
                width: 120,
                sortable: true,
                filter: "agNumberColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Ширина",
                field: "width",
                colId: "width",
                width: 120,
                sortable: true,
                filter: "agNumberColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Высота",
                field: "height",
                colId: "height",
                width: 120,
                sortable: true,
                filter: "agNumberColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Описание",
                field: "description",
                colId: "description",
                flex: 2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Создано",
                field: "createdAt",
                colId: "createdAt",
                width: 200,
                sortable: true,
                filter: "agDateColumnFilter",
                floatingFilter: true,
                valueFormatter: ({value}) => {
                    if (!value) return "";
                    const d = typeof value === "string" ? new Date(value) : value;
                    if (!Number.isFinite(d?.getTime?.())) return String(value);
                    return d.toLocaleString("ru-RU", {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                    });
                },
            },
        ],
        [onOpenEditSizeModal, onDeleteSize]
    );

    const mapSortModel = (sm = []) =>
        sm.map((s) => ({colId: s.colId, sort: s.sort}));

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

                    const res = await apiFetch(`${API_BASE}/api/container-sizes/query`, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                        },
                        body: JSON.stringify(body),
                    });

                    if (!res.ok) {
                        const errorMessage = await parseApiError(res, "Ошибка загрузки");
                        toast.error(errorMessage);
                        params.failCallback();
                        return;
                    }

                    const data = await res.json();
                    params.successCallback(data.rows || [], data.lastRow ?? 0);
                } catch (e) {
                    console.error(e);
                    params.failCallback();
                    toast.error("Ошибка сети. Попробуйте ещё раз.");
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
            height="400px"
        />
    );
}
