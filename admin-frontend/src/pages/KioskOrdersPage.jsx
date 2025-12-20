// src/pages/KioskOrdersPage.jsx

import { useCallback, useMemo, useState } from "react"
import { Button } from "@/components/ui/button"
import { ChevronsUpDown, Plus } from "lucide-react"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { toast } from "sonner"
import { API_BASE } from "../../cfg.js"
import KioskOrdersTable from "@/components/tableData/KioskOrdersTable.jsx"
import { apiFetch } from "@/lib/apiClient.js"

import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem } from "@/components/ui/command"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Input } from "@/components/ui/input"

export default function KioskOrdersPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [activeOrder, setActiveOrder] = useState(null)

    // поля формы
    const [garbagePointId, setGarbagePointId] = useState(null)
    const [containerSizeId, setContainerSizeId] = useState(null)
    const [fractionId, setFractionId] = useState(null)
    const [weight, setWeight] = useState("")
    const [status, setStatus] = useState("CREATED")

    // таблица
    const [refreshGrid, setRefreshGrid] = useState(() => () => {})
    const [tableControls, setTableControls] = useState(null)

    // справочники
    const [gpOptions, setGpOptions] = useState([])
    const [csOptions, setCsOptions] = useState([])
    const [fractionOptions, setFractionOptions] = useState([])

    const [isGpLoading, setIsGpLoading] = useState(false)
    const [isCsLoading, setIsCsLoading] = useState(false)
    const [isFractionLoading, setIsFractionLoading] = useState(false)

    const [isGpPopoverOpen, setIsGpPopoverOpen] = useState(false)
    const [isCsPopoverOpen, setIsCsPopoverOpen] = useState(false)
    const [isFractionPopoverOpen, setIsFractionPopoverOpen] = useState(false)

    // FIX: флаги “уже пытались загрузить” (даже если пришёл пустой список)
    const [gpsFetched, setGpsFetched] = useState(false)
    const [csFetched, setCsFetched] = useState(false)
    const [fractionsFetched, setFractionsFetched] = useState(false)

    const resetForm = () => {
        setActiveOrder(null)
        setGarbagePointId(null)
        setContainerSizeId(null)
        setFractionId(null)
        setWeight("")
        setStatus("CREATED")
    }

    // ============= Загрузка справочных данных (без бесконечной загрузки) =============

    const fetchGarbagePoints = useCallback(async () => {
        if (gpsFetched || isGpLoading) return

        setIsGpLoading(true)
        try {
            const body = {
                startRow: 0,
                endRow: 100,
                sortModel: [{ colId: "createdAt", sort: "desc" }],
                filterModel: {},
            }

            const res = await apiFetch(`${API_BASE}/api/garbage-points/query`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            })

            if (!res.ok) {
                const text = await res.text().catch(() => "")
                console.error("Не удалось получить точки сбора", res.status, res.statusText, text)
                setGpOptions([])
                return
            }

            const data = await res.json()
            setGpOptions(Array.isArray(data.rows) ? data.rows : [])
        } catch (e) {
            console.error("Ошибка загрузки точек сбора", e)
            setGpOptions([])
        } finally {
            setIsGpLoading(false)
            setGpsFetched(true)
        }
    }, [gpsFetched, isGpLoading])

    const fetchContainerSizes = useCallback(async () => {
        if (csFetched || isCsLoading) return

        setIsCsLoading(true)
        try {
            const body = {
                startRow: 0,
                endRow: 100,
                sortModel: [],
                filterModel: {},
            }

            const res = await apiFetch(`${API_BASE}/api/container-sizes/query`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            })

            if (!res.ok) {
                const text = await res.text().catch(() => "")
                console.error("Не удалось получить размеры контейнеров", res.status, res.statusText, text)
                setCsOptions([])
                return
            }

            const data = await res.json()
            setCsOptions(Array.isArray(data.rows) ? data.rows : [])
        } catch (e) {
            console.error("Ошибка загрузки размеров контейнеров", e)
            setCsOptions([])
        } finally {
            setIsCsLoading(false)
            setCsFetched(true)
        }
    }, [csFetched, isCsLoading])

    const fetchFractions = useCallback(async () => {
        if (fractionsFetched || isFractionLoading) return

        setIsFractionLoading(true)
        try {
            const body = {
                startRow: 0,
                endRow: 100,
                sortModel: [],
                filterModel: {},
            }

            const res = await apiFetch(`${API_BASE}/api/fractions/query`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            })

            if (!res.ok) {
                const text = await res.text().catch(() => "")
                console.error("Не удалось получить фракции", res.status, res.statusText, text)
                setFractionOptions([])
                return
            }

            const data = await res.json()
            setFractionOptions(Array.isArray(data.rows) ? data.rows : [])
        } catch (e) {
            console.error("Ошибка загрузки фракций", e)
            setFractionOptions([])
        } finally {
            setIsFractionLoading(false)
            setFractionsFetched(true)
        }
    }, [fractionsFetched, isFractionLoading])

    // ============= Лейблы для комбобоксов =============

    const selectedGpLabel = useMemo(() => {
        if (garbagePointId == null) return ""
        const gp = gpOptions.find((g) => g.id === garbagePointId || g.id === Number(garbagePointId))
        if (!gp) return `Точка #${garbagePointId}`
        const addr = gp.address || "(без адреса)"
        return `${addr} (#${gp.id})`
    }, [garbagePointId, gpOptions])

    const selectedCsLabel = useMemo(() => {
        if (containerSizeId == null) return ""
        const cs = csOptions.find((c) => c.id === containerSizeId || c.id === Number(containerSizeId))
        if (!cs) return `Размер #${containerSizeId}`
        const code = cs.code || ""
        const name = cs.name || ""
        const base = code || name || `ID ${cs.id}`
        return name && code ? `${code} (${name})` : base
    }, [containerSizeId, csOptions])

    const selectedFractionLabel = useMemo(() => {
        if (fractionId == null) return ""
        const fr = fractionOptions.find((f) => f.id === fractionId || f.id === Number(fractionId))
        if (!fr) return `Фракция #${fractionId}`
        const code = fr.code || ""
        const name = fr.name || ""
        const base = name || code || `ID ${fr.id}`
        return code && name ? `${name} (${code})` : base
    }, [fractionId, fractionOptions])

    // ============= Валидация и сохранение =============

    const validate = () => {
        if (!garbagePointId) return "Выберите точку сбора."
        if (!containerSizeId) return "Выберите размер контейнера."
        if (!fractionId) return "Выберите фракцию."
        if (weight !== "" && Number.isNaN(Number(weight))) return "Вес должен быть числом."
        if (weight !== "" && Number(weight) < 0) return "Вес не может быть отрицательным."
        if (!status) return "Выберите статус."
        return null
    }

    const handleSave = async () => {
        const err = validate()
        if (err) {
            toast.warning(err)
            return
        }

        const payload = {
            garbagePointId,
            containerSizeId,
            fractionId,
            weight: weight === "" ? null : Number(weight),
            status,
        }

        const isEdit = Boolean(activeOrder?.id)
        const url = isEdit
            ? `${API_BASE}/api/kiosk-orders/${activeOrder.id}`
            : `${API_BASE}/api/kiosk-orders`

        try {
            const res = await apiFetch(url, {
                method: isEdit ? "PUT" : "POST",
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
            console.error("Ошибка сохранения заказа киоска", e)
            toast.error("Ошибка сохранения. Попробуйте ещё раз.")
        }
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm()
    }

    const handleOpenEditOrderModal = useCallback((row) => {
        setActiveOrder(row)

        setGarbagePointId(row?.garbagePointId ?? row?.garbage_point_id ?? row?.garbagePoint?.id ?? null)
        setContainerSizeId(row?.containerSizeId ?? row?.container_size_id ?? row?.containerSize?.id ?? null)
        setFractionId(row?.fractionId ?? row?.fraction_id ?? row?.fraction?.id ?? null)
        setWeight(
            row?.weight !== undefined && row?.weight !== null
                ? String(row.weight)
                : "",
        )
        setStatus(row?.status ?? "CREATED")

        setIsDialogOpen(true)
    }, [])

    const handleDeleteOrder = useCallback(
        async (row) => {
            if (!row?.id) return
            if (!window.confirm(`Удалить заказ киоска #${row.id}?`)) return

            try {
                const res = await apiFetch(`${API_BASE}/api/kiosk-orders/${row.id}`, {
                    method: "DELETE",
                })

                if (res.ok) {
                    toast.success("Удалено")
                    refreshGrid?.()
                } else {
                    const errorData = await res.json().catch(() => ({}))
                    toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`)
                }
            } catch (e) {
                console.error("Ошибка удаления заказа киоска", e)
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
                        <h1 className="text-2xl font-semibold leading-none tracking-tight">Заказы киосков</h1>
                        <p className="text-sm text-muted-foreground">Заявки от киосков на вывоз контейнеров.</p>
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
                        Новый заказ
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <KioskOrdersTable
                        onOpenEditOrderModal={handleOpenEditOrderModal}
                        onDeleteOrder={handleDeleteOrder}
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        onReadyControls={(controls) => setTableControls(controls)}
                    />
                </div>
            </div>

            <Dialog
                open={isDialogOpen}
                onOpenChange={(open) => {
                    setIsDialogOpen(open)

                    // (опционально) если хочешь при каждом открытии модалки заново подтягивать справочники:
                    // if (open) {
                    //   setGpsFetched(false); setGpOptions([]);
                    //   setCsFetched(false); setCsOptions([]);
                    //   setFractionsFetched(false); setFractionOptions([]);
                    // }
                }}
            >
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>{activeOrder ? "Редактирование заказа" : "Новый заказ киоска"}</DialogTitle>
                        <DialogDescription>
                            Выберите точку сбора, размер контейнера и фракцию — заказ будет сохранён в системе.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            {/* Точка сбора */}
                            <div className="space-y-2">
                                <Label>Точка сбора</Label>
                                <Popover
                                    open={isGpPopoverOpen}
                                    onOpenChange={(open) => {
                                        setIsGpPopoverOpen(open)
                                        if (open) fetchGarbagePoints()
                                    }}
                                >
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" role="combobox" className="w-full justify-between">
                                            {selectedGpLabel || "Выберите точку сбора"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[420px] p-0">
                                        <Command>
                                            <CommandInput placeholder="Найти точку по адресу..." />
                                            <CommandEmpty>{isGpLoading ? "Загрузка..." : "Ничего не найдено"}</CommandEmpty>
                                            <CommandGroup>
                                                {gpOptions.map((g) => (
                                                    <CommandItem
                                                        key={g.id}
                                                        value={`${g.address || ""} #${g.id}`}
                                                        onSelect={() => {
                                                            setGarbagePointId(g.id)
                                                            setIsGpPopoverOpen(false)
                                                        }}
                                                    >
                                                        <span className="mr-2 text-muted-foreground">#{g.id}</span>
                                                        <span>{g.address || "(без адреса)"}</span>
                                                    </CommandItem>
                                                ))}
                                            </CommandGroup>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
                            </div>

                            {/* Размер контейнера */}
                            <div className="space-y-2">
                                <Label>Размер контейнера</Label>
                                <Popover
                                    open={isCsPopoverOpen}
                                    onOpenChange={(open) => {
                                        setIsCsPopoverOpen(open)
                                        if (open) fetchContainerSizes()
                                    }}
                                >
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" role="combobox" className="w-full justify-between">
                                            {selectedCsLabel || "Выберите размер контейнера"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[420px] p-0">
                                        <Command>
                                            <CommandInput placeholder="Найти размер..." />
                                            <CommandEmpty>{isCsLoading ? "Загрузка..." : "Ничего не найдено"}</CommandEmpty>
                                            <CommandGroup>
                                                {csOptions.map((c) => (
                                                    <CommandItem
                                                        key={c.id}
                                                        value={`${c.code || ""} ${c.name || ""}`.trim() || `#${c.id}`}
                                                        onSelect={() => {
                                                            setContainerSizeId(c.id)
                                                            setIsCsPopoverOpen(false)
                                                        }}
                                                    >
                                                        <span className="mr-2 text-muted-foreground">#{c.id}</span>
                                                        <span>{c.code || c.name || "(без имени)"}</span>
                                                        {c.name && c.code && (
                                                            <span className="ml-2 text-xs text-muted-foreground">({c.name})</span>
                                                        )}
                                                    </CommandItem>
                                                ))}
                                            </CommandGroup>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
                            </div>

                            {/* Фракция */}
                            <div className="space-y-2">
                                <Label>Фракция</Label>
                                <Popover
                                    open={isFractionPopoverOpen}
                                    onOpenChange={(open) => {
                                        setIsFractionPopoverOpen(open)
                                        if (open) fetchFractions()
                                    }}
                                >
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" role="combobox" className="w-full justify-between">
                                            {selectedFractionLabel || "Выберите фракцию"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-[420px] p-0">
                                        <Command>
                                            <CommandInput placeholder="Найти фракцию..." />
                                            <CommandEmpty>{isFractionLoading ? "Загрузка..." : "Ничего не найдено"}</CommandEmpty>
                                            <CommandGroup>
                                                {fractionOptions.map((f) => (
                                                    <CommandItem
                                                        key={f.id}
                                                        value={`${f.name || ""} ${f.code || ""}`.trim() || `#${f.id}`}
                                                        onSelect={() => {
                                                            setFractionId(f.id)
                                                            setIsFractionPopoverOpen(false)
                                                        }}
                                                    >
                                                        <span className="mr-2 text-muted-foreground">#{f.id}</span>
                                                        <span>{f.name || "(без имени)"}</span>
                                                        {f.code && <span className="ml-2 text-xs text-muted-foreground">({f.code})</span>}
                                                    </CommandItem>
                                                ))}
                                            </CommandGroup>
                                        </Command>
                                    </PopoverContent>
                                </Popover>
                            </div>

                            {/* Вес */}
                            <div className="space-y-2">
                                <Label htmlFor="weight">Вес (кг)</Label>
                                <Input
                                    id="weight"
                                    type="number"
                                    min={0}
                                    step="0.01"
                                    placeholder="Например, 12.5"
                                    value={weight}
                                    onChange={(e) => setWeight(e.target.value)}
                                />
                            </div>

                            {/* Статус */}
                            <div className="space-y-2">
                                <Label htmlFor="status">Статус</Label>
                                <Select value={status} onValueChange={setStatus}>
                                    <SelectTrigger id="status" className="w-full">
                                        <SelectValue placeholder="Выберите статус" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="CREATED">Создан</SelectItem>
                                        <SelectItem value="CONFIRMED">Подтверждён</SelectItem>
                                        <SelectItem value="CANCELLED">Отменён</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>
                    </div>

                    <DialogFooter className="mt-4">
                        <Button type="button" variant="outline" onClick={handleCancel}>
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
