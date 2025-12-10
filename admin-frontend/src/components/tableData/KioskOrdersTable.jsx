// src/components/tableData/KioskOrdersTable.jsx

import React, {useCallback, useMemo, useRef} from "react"
import {AgGridTable} from "@/components/tableData/AgGridTable.jsx"
import {API_BASE} from "../../../cfg.js"
import {Button} from "@/components/ui/button"
import {MoreHorizontal} from "lucide-react"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from "@/components/ui/dropdown-menu"

export default function KioskOrdersTable({
                                             onOpenEditOrderModal,
                                             onDeleteOrder,
                                             onReadyRefresh,
                                             onReadyControls,
                                         }) {
    const gridApiRef = useRef(null)

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
                                onClick={() => onOpenEditOrderModal?.(p.data)}
                            >
                                Редактировать
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => onDeleteOrder?.(p.data)}
                            >
                                Удалить
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                ),
            },
            {
                headerName: "Точка сбора",
                field: "garbagePointAddress",
                colId: "garbagePointAddress",
                flex: 1.6,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
                valueFormatter: ({data}) => {
                    if (!data) return ""
                    const addr = data.garbagePointAddress || data.garbage_point_address || data.address
                    const id = data.garbagePointId || data.garbage_point_id
                    if (!addr && !id) return ""
                    if (addr && id) return `${addr} (#${id})`
                    if (addr) return addr
                    return `Точка #${id}`
                },
            },
            {
                headerName: "Размер контейнера",
                field: "containerSizeName",
                colId: "containerSizeName",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
                valueFormatter: ({data}) => {
                    if (!data) return ""
                    const name =
                        data.containerSizeName ||
                        data.container_size_name ||
                        data.containerSize?.name
                    const code =
                        data.containerSizeCode ||
                        data.container_size_code ||
                        data.containerSize?.code
                    if (name && code) return `${code} (${name})`
                    return name || code || ""
                },
            },
            {
                headerName: "Фракция",
                field: "fractionName",
                colId: "fractionName",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
                valueFormatter: ({data}) => {
                    if (!data) return ""
                    const name =
                        data.fractionName ||
                        data.fraction_name ||
                        data.fraction?.name
                    const code =
                        data.fractionCode ||
                        data.fraction_code ||
                        data.fraction?.code
                    if (name && code) return `${name} (${code})`
                    return name || code || ""
                },
            },
            {
                headerName: "Статус",
                field: "status",
                colId: "status",
                width: 160,
                sortable: true,
                filter: "agSetColumnFilter",
                floatingFilter: true,
                valueFormatter: ({value}) => {
                    if (!value) return ""
                    switch (value) {
                        case "CREATED":
                            return "Создан"
                        case "CONFIRMED":
                            return "Подтверждён"
                        case "CANCELLED":
                            return "Отменён"
                        default:
                            return String(value)
                    }
                },
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
                    if (!value) return ""
                    const d = typeof value === "string" ? new Date(value) : value
                    if (!Number.isFinite(d?.getTime?.())) return String(value)
                    return d.toLocaleString("ru-RU", {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                    })
                },
            },
        ],
        [onOpenEditOrderModal, onDeleteOrder],
    )

    const mapSortModel = (sm = []) =>
        sm.map((s) => ({colId: s.colId, sort: s.sort}))

    const makeDatasource = useCallback(
        () => ({
            getRows: async (params) => {
                try {
                    const body = {
                        startRow: params.startRow,
                        endRow: params.endRow,
                        sortModel: mapSortModel(params.sortModel),
                        filterModel: params.filterModel || {},
                    }

                    const res = await fetch(`${API_BASE}/api/kiosk-orders/query`, {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                        },
                        body: JSON.stringify(body),
                    })

                    if (!res.ok) {
                        console.error("Ошибка загрузки заказов киосков", res.status, res.statusText)
                        params.failCallback()
                        return
                    }

                    const data = await res.json() // { rows, lastRow }
                    params.successCallback(data.rows || [], data.lastRow ?? 0)
                } catch (e) {
                    console.error(e)
                    params.failCallback()
                }
            },
        }),
        [],
    )

    const setDatasource = useCallback(() => {
        if (!gridApiRef.current) return
        const ds = makeDatasource()
        gridApiRef.current.setGridOption("datasource", ds)
        gridApiRef.current.purgeInfiniteCache()
    }, [makeDatasource])

    const exposeRefresh = useCallback(() => {
        onReadyRefresh?.(() => {
            if (!gridApiRef.current) return
            gridApiRef.current.refreshInfiniteCache()
        })
    }, [onReadyRefresh])

    const exposeControls = useCallback(() => {
        if (!onReadyControls) return
        const api = gridApiRef.current
        if (!api) return

        const clearSort = () => {
            api.applyColumnState?.({defaultState: {sort: null, sortIndex: null}})
            api.setGridOption?.("sortModel", null)
        }

        const setAndGo = (filterModel) => {
            api.setFilterModel(filterModel || null)
            api.onFilterChanged()
            api.purgeInfiniteCache()
            api.ensureIndexVisible(0)
        }

        onReadyControls({
            refresh: () => api.refreshInfiniteCache(),
            clearAll: () => {
                api.setFilterModel(null)
                api.onFilterChanged()
                clearSort()
                api.purgeInfiniteCache()
                api.ensureIndexVisible(0)
            },
            filterOnlyCreated: () => {
                setAndGo({
                    status: {filterType: "set", values: ["CREATED"]},
                })
            },
            filterOnlyConfirmed: () => {
                setAndGo({
                    status: {filterType: "set", values: ["CONFIRMED"]},
                })
            },
        })
    }, [onReadyControls])

    const onGridReady = useCallback(
        (e) => {
            gridApiRef.current = e.api
            setDatasource()
            exposeRefresh()
            exposeControls()
        },
        [setDatasource, exposeRefresh, exposeControls],
    )

    const onFilterChanged = useCallback(() => {
        gridApiRef.current?.purgeInfiniteCache()
        gridApiRef.current?.ensureIndexVisible(0)
    }, [])

    const onSortChanged = useCallback(() => {
        gridApiRef?.purgeInfiniteCache?.()
        gridApiRef?.ensureIndexVisible?.(0)
    }, [])

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
                onFilterChanged,
                onSortChanged,
            }}
            height="500px"
        />
    )
}
