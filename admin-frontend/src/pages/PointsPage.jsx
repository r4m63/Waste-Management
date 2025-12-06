// pages/PointsPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Button} from "@/components/ui/button"
import {Plus, ChevronsUpDown} from "lucide-react"
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
import GarbagePointsTable from "@/components/data/GarbagePointsTable.jsx"
import {API_BASE} from "../../cfg.js"
import {toast} from "sonner"

import {
    Popover,
    PopoverTrigger,
    PopoverContent,
} from "@/components/ui/popover"
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
} from "@/components/ui/command"

export default function PointsPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)

    // null — создание, иначе редактирование
    const [activePoint, setActivePoint] = useState(null)

    const [address, setAddress] = useState("")
    const [capacity, setCapacity] = useState("")
    const [isOpen, setIsOpen] = useState(true)
    const [lat, setLat] = useState("")
    const [lon, setLon] = useState("")
    const [kioskId, setKioskId] = useState(null)

    // данные для таблицы
    const [refreshGrid, setRefreshGrid] = useState(() => () => {})
    const [tableControls, setTableControls] = useState(null)

    // данные для combobox киосков
    const [kioskOptions, setKioskOptions] = useState([])
    const [isKioskLoading, setIsKioskLoading] = useState(false)
    const [isKioskPopoverOpen, setIsKioskPopoverOpen] = useState(false)

    const resetForm = () => {
        setAddress("")
        setCapacity("")
        setIsOpen(true)
        setLat("")
        setLon("")
        setKioskId(null)
        setActivePoint(null)
        // kioskOptions оставляем — пригодятся для следующего открытия
    }

    const validate = () => {
        if (!address.trim()) return "Заполните адрес."
        if (!capacity || Number(capacity) <= 0) return "Вместимость должна быть > 0."
        if (lat !== "" && (Number(lat) < -90 || Number(lat) > 90)) return "Lat должна быть между -90 и 90."
        if (lon !== "" && (Number(lon) < -180 || Number(lon) > 180)) return "Lon должна быть между -180 и 180."
        if (kioskId !== null && Number(kioskId) <= 0) return "kioskId должно быть > 0."
        return null
    }

    const handleSave = async () => {
        const err = validate()
        if (err) {
            toast.warning(err)
            return
        }

        const payload = {
            id: activePoint?.id ?? null,
            address: address.trim(),
            capacity: capacity === "" ? null : Number(capacity),
            open: isOpen,
            lat: lat === "" ? null : Number(lat),
            lon: lon === "" ? null : Number(lon),
            kioskId: kioskId ?? null, // 👈 только kiosk_id
        }

        const isEdit = Boolean(activePoint?.id)
        const url = isEdit
            ? `${API_BASE}/api/garbage-points/${activePoint.id}`
            : `${API_BASE}/api/garbage-points`

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
            console.error("Ошибка сохранения точки", e)
            toast.error("Ошибка сохранения. Попробуйте ещё раз.")
        }
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm()
    }

    const handleOpenEditPointModal = useCallback((row) => {
        // TODO: тут потом заполнишь состояние для редактирования
        console.log("edit row", row)
    }, [])

    // === Загрузка списка киосков (users с role=KIOSK) для комбобокса ===

    const fetchKiosks = useCallback(async () => {
        // если уже загружали — не дёргаем бэк ещё раз
        if (kioskOptions.length > 0 || isKioskLoading) return

        setIsKioskLoading(true)
        try {
            const body = {
                startRow: 0,
                endRow: 50,
                sortModel: [{colId: "createdAt", sort: "desc"}],
                // можно подтянуть только активные, если хочешь
                filterModel: {
                    role: {filterType: "text", type: "equals", filter: "KIOSK"},
                },
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
                console.error("Не удалось получить список киосков", res.status, res.statusText)
                return
            }

            const data = await res.json()
            // ожидаем data.rows: [{id,name,login,active,...}]
            setKioskOptions(data.rows || [])
        } catch (e) {
            console.error("Ошибка загрузки киосков", e)
        } finally {
            setIsKioskLoading(false)
        }
    }, [kioskOptions.length, isKioskLoading])

    // При открытии диалога — один раз подгружаем киоски
    useEffect(() => {
        if (isDialogOpen) {
            fetchKiosks()
        }
    }, [isDialogOpen, fetchKiosks])

    const selectedKioskLabel = useMemo(() => {
        if (kioskId == null) return ""
        const found = kioskOptions.find((k) => k.id === kioskId || k.id === Number(kioskId))
        if (!found) return `ID ${kioskId}`
        const name = found.name || "(без имени)"
        return found.login ? `${name} (${found.login})` : name
    }, [kioskId, kioskOptions])

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
                        onClick={() => {
                            resetForm()
                            setIsDialogOpen(true)
                        }}
                    >
                        <Plus className="h-4 w-4"/> Добавить точку
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <GarbagePointsTable
                        onOpenEditPointModal={handleOpenEditPointModal}
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        onReadyControls={(controls) => setTableControls(controls)}
                    />
                </div>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activePoint ? "Редактирование точки" : "Новая точка сбора"}
                        </DialogTitle>
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

                            {/* Combobox для kiosk_id */}
                            <div className="space-y-2">
                                <Label>Киоск (user с ролью KIOSK)</Label>
                                <Popover
                                    open={isKioskPopoverOpen}
                                    onOpenChange={(open) => {
                                        setIsKioskPopoverOpen(open)
                                        if (open) {
                                            fetchKiosks()
                                        }
                                    }}
                                >
                                    <PopoverTrigger asChild>
                                        <Button
                                            variant="outline"
                                            role="combobox"
                                            className="w-full justify-between"
                                        >
                                            {selectedKioskLabel || "Выберите киоск"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50"/>
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[420px] p-0">
                                        <Command>
                                            <CommandInput
                                                placeholder="Найти киоск..."
                                                // Command сам фильтрует по value CommandItem'ов
                                            />
                                            <CommandEmpty>
                                                {isKioskLoading
                                                    ? "Загрузка..."
                                                    : "Ничего не найдено"}
                                            </CommandEmpty>
                                            <CommandGroup>
                                                {kioskOptions.map((k) => (
                                                    <CommandItem
                                                        key={k.id}
                                                        value={`${k.name || ""} ${k.login || ""}`.trim() || `#${k.id}`}
                                                        onSelect={() => {
                                                            setKioskId(k.id)
                                                            setIsKioskPopoverOpen(false)
                                                        }}
                                                    >
                                                        <span className="mr-2 text-muted-foreground">
                                                            #{k.id}
                                                        </span>
                                                        <span>{k.name || "(без имени)"}</span>
                                                        {k.login && (
                                                            <span
                                                                className="ml-2 text-xs text-muted-foreground">
                                                                ({k.login})
                                                            </span>
                                                        )}
                                                    </CommandItem>
                                                ))}
                                            </CommandGroup>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
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
