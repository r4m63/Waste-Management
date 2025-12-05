// pages/PointsPage.jsx

import {useMemo} from "react"
import {AgGridTable} from "@/components/data/AgGridTable.jsx"

const mockRowData = [
    {
        id: "P-101",
        name: "Контейнерная площадка #101",
        address: "ул. Ленина, 14",
        status: "Активна",
        fill: 32,
        nextPickup: "Сегодня, 15:30",
    },
    {
        id: "P-102",
        name: "ТОС «Северный»",
        address: "пр-т Мира, 56",
        status: "Требует визит",
        fill: 86,
        nextPickup: "Сегодня, 12:00",
    },
    {
        id: "P-118",
        name: "БЦ «Орион»",
        address: "ул. Гагарина, 9",
        status: "В очереди",
        fill: 64,
        nextPickup: "Сегодня, 18:45",
    },
    {
        id: "P-201",
        name: "ТПУ «Южный»",
        address: "ул. Вокзальная, 3",
        status: "Активна",
        fill: 41,
        nextPickup: "Завтра, 09:00",
    },
    {
        id: "P-315",
        name: "ТЦ «Мега Сити»",
        address: "ул. Индустриальная, 21",
        status: "Переполнена",
        fill: 96,
        nextPickup: "Через 30 мин",
    },
]

export default function PointsPage() {
    const columnDefs = useMemo(() => [
        {field: "id", headerName: "ID", maxWidth: 100},
        {field: "name", headerName: "Название", flex: 1.4},
        {field: "address", headerName: "Адрес", flex: 1.6},
        {field: "status", headerName: "Статус", flex: 1},
        {
            field: "fill",
            headerName: "Заполненность",
            filter: "agNumberColumnFilter",
            valueFormatter: ({value}) => `${value}%`,
            maxWidth: 160,
        },
        {field: "nextPickup", headerName: "След. вывоз", flex: 1},
    ], [])

    const defaultColDef = useMemo(() => ({
        resizable: true,
        sortable: true,
        filter: true,
    }), [])

    return (
        <div className="flex flex-1 min-h-0 flex-col gap-4">
            <div className="flex flex-col gap-2">
                <h1 className="text-2xl font-semibold leading-none tracking-tight">Точки</h1>
                <p className="text-sm text-muted-foreground">
                    Список точек сбора с быстрым предпросмотром карты и статусов.
                </p>
            </div>

            <div className="flex-1 min-h-[400px]">
                <AgGridTable
                    columnDefs={columnDefs}
                    rowData={mockRowData}
                    defaultColDef={defaultColDef}
                    gridOptions={{
                        rowSelection: "multiple",
                    }}
                    height="500px"
                />
            </div>
        </div>
    )
}
