// src/components/tableData/ShiftsTable.jsx

import React, {useMemo} from "react"
import {AgGridTable} from "@/components/tableData/AgGridTable.jsx"
import {Clock, CheckCircle2} from "lucide-react"

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

const StatusBadge = ({status}) => {
    if (status === "open") {
        return (
            <span className="inline-flex items-center gap-1 rounded-full bg-green-500/15 px-2 py-1 text-xs font-medium text-green-400">
                <Clock className="h-3 w-3"/>
                Открыта
            </span>
        )
    }
    return (
        <span className="inline-flex items-center gap-1 rounded-full bg-slate-500/15 px-2 py-1 text-xs font-medium text-slate-400">
            <CheckCircle2 className="h-3 w-3"/>
            Закрыта
        </span>
    )
}

const calculateDuration = (openedAt, closedAt) => {
    if (!openedAt) return "—"
    const start = new Date(openedAt)
    const end = closedAt ? new Date(closedAt) : new Date()
    if (!Number.isFinite(start.getTime())) return "—"
    
    const diffMs = end.getTime() - start.getTime()
    const hours = Math.floor(diffMs / (1000 * 60 * 60))
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
    
    if (hours > 0) {
        return `${hours}ч ${minutes}м`
    }
    return `${minutes}м`
}

export default function ShiftsTable({shifts}) {
    const columnDefs = useMemo(() => [
        {headerName: "ID", field: "id", width: 80, filter: "agNumberColumnFilter"},
        {
            headerName: "Статус",
            field: "status",
            width: 130,
            cellRenderer: (p) => <StatusBadge status={p.value}/>,
            valueFormatter: ({value}) => value === "open" ? "Открыта" : "Закрыта",
        },
        {
            headerName: "Водитель",
            field: "driverName",
            flex: 1.2,
            valueFormatter: ({value, data}) => value || data?.driverLogin || `ID ${data?.driverId}` || "—",
        },
        {
            headerName: "ТС",
            field: "vehiclePlate",
            width: 140,
            valueFormatter: ({value, data}) => value || (data?.vehicleId ? `ID ${data.vehicleId}` : "—"),
        },
        {
            headerName: "Открыта",
            field: "openedAt",
            width: 160,
            valueFormatter: ({value}) => formatDate(value),
        },
        {
            headerName: "Закрыта",
            field: "closedAt",
            width: 160,
            valueFormatter: ({value}) => formatDate(value),
        },
        {
            headerName: "Длительность",
            colId: "duration",
            width: 130,
            valueGetter: (p) => calculateDuration(p.data?.openedAt, p.data?.closedAt),
        },
    ], [])

    return (
        <AgGridTable
            columnDefs={columnDefs}
            rowData={shifts}
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

