// src/components/tableData/IncidentsTable.jsx

import React, {useMemo} from "react"
import {AgGridTable} from "@/components/tableData/AgGridTable.jsx"
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu"
import {Button} from "@/components/ui/button"
import {CheckCircle, MoreHorizontal} from "lucide-react"

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

const INCIDENT_TYPE_LABELS = {
    access_denied: "Нет доступа",
    traffic: "Пробки",
    vehicle_issue: "Проблема с ТС",
    overload: "Перегруз",
    other: "Другое",
}

const StatusBadge = ({resolved}) => {
    if (resolved) {
        return (
            <span className="inline-flex items-center gap-1 rounded-full bg-green-500/15 px-2 py-1 text-xs font-medium text-green-400">
                <CheckCircle className="h-3 w-3"/>
                Решён
            </span>
        )
    }
    return (
        <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/15 px-2 py-1 text-xs font-medium text-amber-400">
            Открыт
        </span>
    )
}

export default function IncidentsTable({incidents, onResolve}) {
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
                        {!p.data?.resolved ? (
                            <DropdownMenuItem onClick={() => onResolve?.(p.data)}>
                                <CheckCircle className="mr-2 h-4 w-4"/>
                                Отметить решённым
                            </DropdownMenuItem>
                        ) : (
                            <DropdownMenuItem disabled>
                                Уже решён
                            </DropdownMenuItem>
                        )}
                    </DropdownMenuContent>
                </DropdownMenu>
            ),
        },
        {headerName: "ID", field: "id", width: 80, filter: "agNumberColumnFilter"},
        {
            headerName: "Статус",
            field: "resolved",
            width: 120,
            cellRenderer: (p) => <StatusBadge resolved={p.value}/>,
            valueFormatter: ({value}) => value ? "Решён" : "Открыт",
        },
        {
            headerName: "Тип",
            field: "type",
            width: 150,
            valueFormatter: ({value}) => INCIDENT_TYPE_LABELS[value] || value || "—",
        },
        {
            headerName: "Остановка",
            field: "stopAddress",
            flex: 1.2,
            valueFormatter: ({value, data}) => value || `Остановка #${data?.stopId}` || "—",
        },
        {
            headerName: "Маршрут",
            field: "routeId",
            width: 100,
            valueFormatter: ({value}) => value ? `#${value}` : "—",
        },
        {
            headerName: "Описание",
            field: "description",
            flex: 1.5,
            valueFormatter: ({value}) => value || "—",
        },
        {
            headerName: "Создатель",
            field: "createdByName",
            width: 150,
            valueFormatter: ({value, data}) => value || data?.createdByLogin || "—",
        },
        {
            headerName: "Создан",
            field: "createdAt",
            width: 160,
            valueFormatter: ({value}) => formatDate(value),
        },
        {
            headerName: "Решён",
            field: "resolvedAt",
            width: 160,
            valueFormatter: ({value}) => formatDate(value),
        },
    ], [onResolve])

    return (
        <AgGridTable
            columnDefs={columnDefs}
            rowData={incidents}
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

