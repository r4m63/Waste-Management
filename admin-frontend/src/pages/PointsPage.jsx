// pages/PointsPage.jsx

import {useMemo, useState} from "react"
import {AgGridTable} from "@/components/data/AgGridTable.jsx"
import {Button} from "@/components/ui/button"
import {MoreHorizontal, Plus} from "lucide-react"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import {Label} from "@/components/ui/label"
import {Input} from "@/components/ui/input"
import {Switch} from "@/components/ui/switch"

const mockRowData = [
    {
        id: 1,
        address: "ул. Ленина, 14",
        capacity: 10,
        isOpen: true,
        lat: 59.9386,
        lon: 30.3141,
        createdAt: "2025-12-01T10:15:00+03:00",
        adminId: 101,
        userId: 201,
    },
    {
        id: 2,
        address: "пр-т Мира, 56",
        capacity: 6,
        isOpen: false,
        lat: 59.945,
        lon: 30.32,
        createdAt: "2025-12-02T09:30:00+03:00",
        adminId: 102,
        userId: 202,
    },
    {
        id: 3,
        address: "ул. Гагарина, 9",
        capacity: 8,
        isOpen: true,
        lat: 59.93,
        lon: 30.29,
        createdAt: "2025-12-03T14:05:00+03:00",
        adminId: 103,
        userId: 203,
    },
    {
        id: 4,
        address: "ул. Вокзальная, 3",
        capacity: 12,
        isOpen: true,
        lat: 59.92,
        lon: 30.35,
        createdAt: "2025-12-04T08:45:00+03:00",
        adminId: 104,
        userId: 204,
    },
    {
        id: 5,
        address: "ул. Индустриальная, 21",
        capacity: 15,
        isOpen: false,
        lat: 59.94,
        lon: 30.33,
        createdAt: "2025-12-05T18:20:00+03:00",
        adminId: 105,
        userId: 205,
    },
]

export default function PointsPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)

    const [address, setAddress] = useState("")
    const [capacity, setCapacity] = useState("")
    const [isOpen, setIsOpen] = useState(true)
    const [lat, setLat] = useState("")
    const [lon, setLon] = useState("")
    const [adminId, setAdminId] = useState("")
    const [userId, setUserId] = useState("")

    const resetForm = () => {
        setAddress("")
        setCapacity("")
        setIsOpen(true)
        setLat("")
        setLon("")
        setAdminId("")
        setUserId("")
    }

    const handleSave = () => {
        const payload = {
            address: address.trim(),
            capacity: capacity ? Number(capacity) : null,
            isOpen,
            lat: lat ? Number(lat) : null,
            lon: lon ? Number(lon) : null,
            adminId: adminId ? Number(adminId) : null,
            userId: userId ? Number(userId) : null,
        }


        setIsDialogOpen(false)
        resetForm()
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm() // TODO: сохранять состояние и подтягивать его, уведомив в sonner и в sonner кнопку сброса состояния добавить
    }

    const columnDefs = useMemo(
        () => [
            {
                field: "id",
                headerName: "ID",
                maxWidth: 90,
                filter: "agNumberColumnFilter",
            },
            {
                headerName: "Действия",
                colId: "actions",
                maxWidth: 110,
                sortable: false,
                filter: false,
                suppressMenu: true,
                pinned: "left",
                cellRenderer: (params) => {
                    const point = params.data

                    return (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    className="h-8 w-8 p-0"
                                >
                                    <span className="sr-only">Открыть меню</span>
                                    <MoreHorizontal className="h-4 w-4"/>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuItem
                                    onClick={() => console.log("open-map", point)}
                                >
                                    Открыть на карте
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                    onClick={() => console.log("edit", point)}
                                >
                                    Редактировать
                                </DropdownMenuItem>
                                <DropdownMenuSeparator/>
                                <DropdownMenuItem
                                    onClick={() => console.log("toggle-open", point)}
                                >
                                    {point?.isOpen ? "Закрыть точку" : "Открыть точку"}
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    )
                },
            },
            {
                field: "address",
                headerName: "Адрес",
                flex: 1.6,
            },
            {
                field: "capacity",
                headerName: "Вместимость",
                filter: "agNumberColumnFilter",
                maxWidth: 150,
            },
            {
                field: "isOpen",
                headerName: "Открыта",
                maxWidth: 130,
                filter: "agSetColumnFilter",
                valueFormatter: ({value}) =>
                    value === true ? "Да" : value === false ? "Нет" : "",
            },
            {
                field: "lat",
                headerName: "Широта (lat)",
                filter: "agNumberColumnFilter",
                maxWidth: 160,
                valueFormatter: ({value}) =>
                    value != null ? value.toFixed(5) : "",
            },
            {
                field: "lon",
                headerName: "Долгота (lon)",
                filter: "agNumberColumnFilter",
                maxWidth: 160,
                valueFormatter: ({value}) =>
                    value != null ? value.toFixed(5) : "",
            },
            {
                field: "createdAt",
                headerName: "Создано",
                flex: 1.2,
                valueFormatter: ({value}) => {
                    if (!value) return ""
                    const d = new Date(value)
                    if (Number.isNaN(d.getTime())) return value
                    return d.toLocaleString("ru-RU", {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                    })
                },
            },
            {
                field: "adminId",
                headerName: "Админ (ID)",
                filter: "agNumberColumnFilter",
                maxWidth: 140,
            },
            {
                field: "userId",
                headerName: "Пользователь (ID)",
                filter: "agNumberColumnFilter",
                maxWidth: 160,
            },
        ],
        [],
    )

    const defaultColDef = useMemo(
        () => ({
            resizable: true,
            sortable: true,
            filter: true,
        }),
        [],
    )

    return (
        <>
            <div className="flex flex-1 min-h-0 flex-col gap-4">
                <div className="flex items-center justify-between gap-4">
                    <div className="flex flex-col gap-1">
                        <h1 className="text-2xl font-semibold leading-none tracking-tight">
                            Точки сбора
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Список точек сбора мусора.
                        </p>
                    </div>

                    <Button
                        size="sm"
                        className="gap-2"
                        onClick={() => setIsDialogOpen(true)}
                    >
                        <Plus className="h-4 w-4"/>
                        Добавить точку
                    </Button>
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

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Новая точка сбора</DialogTitle>
                        <DialogDescription>
                            Заполните данные точки, они будут сохранены в системе.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="address">Адрес</Label>
                                <Input
                                    id="address"
                                    placeholder="ул. Пример, 10"
                                    value={address}
                                    onChange={(e) => setAddress(e.target.value)}
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="capacity">Вместимость (контейнеров)</Label>
                                    <Input
                                        id="capacity"
                                        type="number"
                                        min={0}
                                        placeholder="Например, 10"
                                        value={capacity}
                                        onChange={(e) => setCapacity(e.target.value)}
                                    />
                                </div>

                                <div className="flex items-center gap-3 pt-6">
                                    <Switch
                                        id="isOpen"
                                        checked={isOpen}
                                        onCheckedChange={(checked) => setIsOpen(checked)}
                                    />
                                    <Label htmlFor="isOpen" className="cursor-pointer">
                                        Точка открыта
                                    </Label>
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="lat">Широта (lat)</Label>
                                    <Input
                                        id="lat"
                                        type="number"
                                        step="0.000001"
                                        placeholder="59.93..."
                                        value={lat}
                                        onChange={(e) => setLat(e.target.value)}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="lon">Долгота (lon)</Label>
                                    <Input
                                        id="lon"
                                        type="number"
                                        step="0.000001"
                                        placeholder="30.31..."
                                        value={lon}
                                        onChange={(e) => setLon(e.target.value)}
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="adminId">Админ (ID)</Label>
                                    <Input
                                        id="adminId"
                                        type="number"
                                        placeholder="ID администратора"
                                        value={adminId}
                                        onChange={(e) => setAdminId(e.target.value)}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="userId">Пользователь (ID)</Label>
                                    <Input
                                        id="userId"
                                        type="number"
                                        placeholder="ID пользователя"
                                        value={userId}
                                        onChange={(e) => setUserId(e.target.value)}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <DialogFooter className="mt-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleCancel}
                        >
                            Отмена
                        </Button>
                        <Button type="button" onClick={handleSave}>
                            Сохранить
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </>
    )
}
