// src/components/tableData/RoutesTable.jsx

import React, {useMemo} from "react"
import {AgGridTable} from "@/components/tableData/AgGridTable.jsx"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu"
import {Button} from "@/components/ui/button"
import {ChevronsUpDown, MapPin, MoreHorizontal} from "lucide-react"

const formatDate = (value) => {
    if (!value) return ""
    const d = new Date(value)
    if (!Number.isFinite(d?.getTime?.())) return value
    return d.toLocaleString("ru-RU", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    })
}

const StopsCell = ({data}) => {
    const stops = data?.stops || []
    if (!stops.length) {
        return <span className="text-muted-foreground">—</span>
    }

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 gap-2 px-2">
                    <span className="text-sm">{stops.length} остановк{stops.length === 1 ? "а" : "и"}</span>
                    <ChevronsUpDown className="h-4 w-4 text-muted-foreground"/>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-72">
                {stops.map((stop) => (
                    <DropdownMenuItem key={stop.id || stop.seqNo} className="flex items-start gap-2">
                        <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary">
                            #{stop.seqNo}
                        </span>
                        <div className="flex flex-col gap-0.5">
                            <div className="flex items-center gap-1 text-sm font-medium">
                                <MapPin className="h-3.5 w-3.5 text-muted-foreground"/>
                                {stop.address || `Точка #${stop.garbagePointId}`}
                            </div>
                            <div className="text-xs text-muted-foreground capitalize">
                                {stop.status || "planned"}
                            </div>
                            {stop.expectedCapacity != null && (
                                <div className="text-xs text-muted-foreground">
                                    Ожид. объём: {stop.expectedCapacity}
                                </div>
                            )}
                        </div>
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    )
}

export default function RoutesTable({routes, onDelete, onAssign}) {
    const columnDefs = useMemo(() => [
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
                        <DropdownMenuItem onClick={() => onAssign?.(p.data)}>
                            Назначить исполнителя
                        </DropdownMenuItem>
                        <DropdownMenuItem
                            className="text-red-600 focus:text-red-700 focus:bg-red-100 dark:focus:bg-red-900/30"
                            onClick={() => onDelete?.(p.data)}
                        >
                            Удалить
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            ),
        },
        {headerName: "ID", field: "id", width: 90, filter: "agNumberColumnFilter"},
        {
            headerName: "Дата",
            field: "plannedDate",
            valueFormatter: ({value}) => (value ? value : ""),
            width: 130,
        },
        {
            headerName: "Статус",
            field: "status",
            width: 140,
            valueFormatter: ({value}) => (value ? String(value) : ""),
        },
        {
            headerName: "Остановки",
            field: "stops",
            cellRenderer: StopsCell,
            sortable: false,
            filter: false,
            width: 200,
        },
        {
            headerName: "Начало",
            field: "plannedStartAt",
            valueFormatter: ({value}) => formatDate(value),
            width: 170,
        },
        {
            headerName: "Окончание",
            field: "plannedEndAt",
            valueFormatter: ({value}) => formatDate(value),
            width: 170,
        },
    ], [])

    return (
        <AgGridTable
            columnDefs={columnDefs}
            rowData={routes}
            defaultColDef={{
                resizable: true,
                sortable: true,
                filter: true,
                floatingFilter: true,
            }}
            gridOptions={{
                animateRows: true,
                pagination: true,
                paginationPageSize: 20,
            }}
            height="70vh"
        />
    )
}
