// src/pages/VehiclesPage.jsx

import { useCallback, useState } from "react"
import { Button } from "@/components/ui/button"
import { Plus } from "lucide-react"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Switch } from "@/components/ui/switch"
import { toast } from "sonner"
import { API_BASE } from "../../cfg.js"
import VehiclesTable from "@/components/tableData/VehiclesTable.jsx"

export default function VehiclesPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [activeVehicle, setActiveVehicle] = useState(null)

    const [plateNumber, setPlateNumber] = useState("")
    const [name, setName] = useState("")
    const [capacity, setCapacity] = useState("")
    const [isActive, setIsActive] = useState(true)

    const [refreshGrid, setRefreshGrid] = useState(() => () => {})
    const [tableControls, setTableControls] = useState(null)

    const resetForm = () => {
        setActiveVehicle(null)
        setPlateNumber("")
        setName("")
        setCapacity("")
        setIsActive(true)
    }

    const validate = () => {
        if (!plateNumber.trim()) return "Укажите госномер транспортного средства."
        if (capacity === "" || Number.isNaN(Number(capacity))) {
            return "Укажите вместимость."
        }
        if (Number(capacity) < 0) return "Вместимость не может быть отрицательной."
        return null
    }

    const handleSave = async () => {
        const err = validate()
        if (err) {
            toast.warning(err)
            return
        }

        const payload = {
            plateNumber: plateNumber.trim(),
            name: name.trim() || null,
            capacity: capacity === "" ? null : Number(capacity),
            active: isActive,
        }

        const isEdit = Boolean(activeVehicle?.id)
        const url = isEdit
            ? `${API_BASE}/api/vehicles/${activeVehicle.id}`
            : `${API_BASE}/api/vehicles`

        try {
            const res = await fetch(url, {
                method: isEdit ? "PUT" : "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(payload),
            })

            if (res.ok) {
                refreshGrid?.()
                setIsDialogOpen(false)
                resetForm()
                toast.success("Сохранено")
            } else {
                const errorData = await res.json().catch(() => ({}))
                toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`)
            }
        } catch (e) {
            console.error("Ошибка сохранения ТС", e)
            toast.error("Ошибка сохранения. Попробуйте ещё раз.")
        }
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm()
    }

    const handleOpenEditVehicleModal = useCallback((row) => {
        setActiveVehicle(row)
        setPlateNumber(row?.plateNumber ?? "")
        setName(row?.name ?? "")
        setCapacity(
            row?.capacity !== undefined && row?.capacity !== null
                ? String(row.capacity)
                : "",
        )
        setIsActive(row?.active ?? true)
        setIsDialogOpen(true)
    }, [])

    const handleDeleteVehicle = useCallback(
        async (row) => {
            if (!row?.id) return
            if (!window.confirm(`Удалить транспортное средство #${row.id}?`)) return

            try {
                const res = await fetch(`${API_BASE}/api/vehicles/${row.id}`, {
                    method: "DELETE",
                    credentials: "include",
                })

                if (res.ok) {
                    toast.success("Удалено")
                    refreshGrid?.()
                } else {
                    const errorData = await res.json().catch(() => ({}))
                    toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`)
                }
            } catch (e) {
                console.error("Ошибка удаления ТС", e)
                toast.error("Ошибка удаления. Попробуйте ещё раз.")
            }
        },
        [refreshGrid],
    )

    return (
        <>
            <div className="flex flex-1 min-h-0 flex-col gap-4">
                <div className="flex items-center justify-between gap-4">
                    <div className="flex flex-col gap-1">
                        <h1 className="text-2xl font-semibold leading-none tracking-tight">
                            Транспортные средства
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Управление автопарком: госномер, имя, вместимость и статус.
                        </p>
                    </div>

                    <Button
                        size="sm"
                        className="gap-2"
                        onClick={() => {
                            resetForm()
                            setIsDialogOpen(true)
                        }}
                    >
                        <Plus className="h-4 w-4" />
                        Добавить ТС
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <VehiclesTable
                        onOpenEditVehicleModal={handleOpenEditVehicleModal}
                        onDeleteVehicle={handleDeleteVehicle}
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        onReadyControls={(controls) => setTableControls(controls)}
                    />
                </div>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activeVehicle ? "Редактирование ТС" : "Новое транспортное средство"}
                        </DialogTitle>
                        <DialogDescription>
                            Заполните данные, они будут сохранены в системе.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="plateNumber">Госномер</Label>
                                <Input
                                    id="plateNumber"
                                    placeholder="А123ВС178"
                                    value={plateNumber}
                                    onChange={(e) => setPlateNumber(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="name">Имя / описание</Label>
                                <Input
                                    id="name"
                                    placeholder="Мусоровоз #1"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="capacity">Вместимость (условные единицы)</Label>
                                    <Input
                                        id="capacity"
                                        type="number"
                                        min={0}
                                        placeholder="Например, 20"
                                        value={capacity}
                                        onChange={(e) => setCapacity(e.target.value)}
                                    />
                                </div>

                                <div className="flex items-center gap-3 pt-6">
                                    <Switch
                                        id="isActive"
                                        checked={isActive}
                                        onCheckedChange={(checked) => setIsActive(checked)}
                                    />
                                    <Label htmlFor="isActive" className="cursor-pointer">
                                        Активно
                                    </Label>
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
