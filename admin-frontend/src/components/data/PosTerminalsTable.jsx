// components/data/PosTerminalsTable.jsx

import React, {useCallback, useMemo, useRef} from "react"
import {AgGridTable} from "@/components/data/AgGridTable.jsx"
import {API_BASE} from "../../../cfg.js"
import {Button} from "@/components/ui/button"
import {MoreHorizontal} from "lucide-react"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,} from "@/components/ui/dropdown-menu"

export default function PosTerminalsTable({
                                              onOpenEditTerminalModal,
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
                                onClick={() => onOpenEditTerminalModal?.(p.data)}
                            >
                                Редактировать
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                onClick={() => console.log("TODO: удалить терминал", p.data.id)}
                            >
                                Удалить
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                ),
            },
            {
                headerName: "Имя",
                field: "name",
                colId: "name",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Логин",
                field: "login",
                colId: "login",
                flex: 1.2,
                sortable: true,
                filter: "agTextColumnFilter",
                floatingFilter: true,
            },
            {
                headerName: "Активен",
                field: "active",
                colId: "active",
                width: 120,
                sortable: true,
                filter: "agSetColumnFilter",
                floatingFilter: true,
                valueFormatter: ({value}) =>
                    value === true ? "Да" : value === false ? "Нет" : "",
            },
            {
                headerName: "Создан",
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
        [onOpenEditTerminalModal],
    )

    const mapSortModel = (sm = []) => sm.map((s) => ({colId: s.colId, sort: s.sort}))

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

                    const res = await fetch(`${API_BASE}/api/kiosk/query`, {
                        method: "POST",
                        credentials: "include",
                        headers: {
                            "Content-Type": "application/json",
                            Accept: "application/json",
                        },
                        body: JSON.stringify(body),
                    })

                    if (!res.ok) {
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
            filterOnlyActive: () => {
                setAndGo({
                    active: {filterType: "set", values: [true]},
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
        gridApiRef.current?.purgeInfiniteCache()
        gridApiRef.current?.ensureIndexVisible(0)
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
