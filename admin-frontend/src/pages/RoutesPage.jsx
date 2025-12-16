// pages/RoutesPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Button} from "@/components/ui/button"
import {Card, CardContent} from "@/components/ui/card"
import {API_BASE} from "../../cfg.js"
import {apiFetch} from "@/lib/apiClient.js"
import {toast} from "sonner"
import {ChevronsUpDown, Loader2, RefreshCw} from "lucide-react"
import RoutesTable from "@/components/tableData/RoutesTable.jsx"
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover"
import {Command, CommandEmpty, CommandGroup, CommandInput, CommandItem} from "@/components/ui/command"

export default function RoutesPage() {
    const [routes, setRoutes] = useState([])
    const [loading, setLoading] = useState(false)
    const [generating, setGenerating] = useState(false)
    const [deleting, setDeleting] = useState(false)
    const [assigning, setAssigning] = useState(false)
    const [assignModalOpen, setAssignModalOpen] = useState(false)
    const [assignRoute, setAssignRoute] = useState(null)
    const [driverOptions, setDriverOptions] = useState([])
    const [isDriverLoading, setIsDriverLoading] = useState(false)
    const [driverFetched, setDriverFetched] = useState(false)
    const [isDriverPopoverOpen, setIsDriverPopoverOpen] = useState(false)
    const [selectedDriverId, setSelectedDriverId] = useState(null)
    const [selectedDriverLabel, setSelectedDriverLabel] = useState("")
    const [plannedStartDate, setPlannedStartDate] = useState("")
    const [plannedStartTime, setPlannedStartTime] = useState("")
    const [plannedEndDate, setPlannedEndDate] = useState("")
    const [plannedEndTime, setPlannedEndTime] = useState("")

    const fetchRoutes = useCallback(async () => {
        setLoading(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/routes`)
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            const data = await res.json()
            setRoutes(Array.isArray(data) ? data : [])
        } catch (e) {
            console.error("Не удалось загрузить маршруты", e)
            toast.error(e.message || "Не удалось загрузить маршруты")
            setRoutes([])
        } finally {
            setLoading(false)
        }
    }, [])

    const handleDelete = async (route) => {
        if (!route?.id) return
        if (!window.confirm(`Удалить маршрут #${route.id}?`)) return
        setDeleting(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/routes/${route.id}`, {method: "DELETE"})
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            toast.success(`Маршрут #${route.id} удалён`)
            await fetchRoutes()
        } catch (e) {
            toast.error(e instanceof Error ? e.message : "Не удалось удалить маршрут")
        } finally {
            setDeleting(false)
        }
    }

    useEffect(() => {
        fetchRoutes()
    }, [fetchRoutes])

    const fetchDrivers = useCallback(async () => {
        if (driverFetched || isDriverLoading) return
        setIsDriverLoading(true)
        try {
            const body = {
                startRow: 0,
                endRow: 200,
                sortModel: [],
                filterModel: {},
            }
            const res = await apiFetch(`${API_BASE}/api/drivers/query`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            })
            if (!res.ok) {
                setDriverOptions([])
                return
            }
            const data = await res.json()
            setDriverOptions(Array.isArray(data.rows) ? data.rows : [])
        } catch (e) {
            console.error("Не удалось загрузить водителей", e)
            setDriverOptions([])
        } finally {
            setIsDriverLoading(false)
            setDriverFetched(true)
        }
    }, [driverFetched, isDriverLoading])

    const handleGenerate = async () => {
        setGenerating(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/routes/auto-generate`, {method: "POST"})
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            await fetchRoutes()
            toast.success("Маршрут создан")
        } catch (e) {
            const msg = e instanceof Error ? e.message : "Не удалось создать маршрут"
            toast.error(msg)
        } finally {
            setGenerating(false)
        }
    }

    const openAssignModal = (route) => {
        setAssignRoute(route)

        const driverLabel = route?.driverId ? `ID ${route.driverId}` : ""
        setSelectedDriverId(route?.driverId ?? null)
        setSelectedDriverLabel(driverLabel)

        const parseDate = (value) => {
            if (!value) return {date: "", time: ""}
            const d = new Date(value)
            if (!Number.isFinite(d?.getTime?.())) return {date: "", time: ""}
            const iso = d.toISOString()
            return {
                date: iso.slice(0, 10),
                time: iso.slice(11, 16),
            }
        }

        const start = parseDate(route?.plannedStartAt)
        const end = parseDate(route?.plannedEndAt)
        setPlannedStartDate(start.date)
        setPlannedStartTime(start.time)
        setPlannedEndDate(end.date)
        setPlannedEndTime(end.time)

        setAssignModalOpen(true)
    }

    const handleAssign = async () => {
        if (!assignRoute?.id) return
        if (!selectedDriverId) {
            toast.error("Укажите водителя")
            return
        }
        setAssigning(true)
        try {
            const buildIso = (date, time) => {
                if (!date) return null
                const t = time || "00:00"
                return new Date(`${date}T${t}`).toISOString()
            }
            const payload = {
                driverId: selectedDriverId,
                plannedStartAt: buildIso(plannedStartDate, plannedStartTime),
                plannedEndAt: buildIso(plannedEndDate, plannedEndTime),
            }
            const res = await apiFetch(`${API_BASE}/api/routes/${assignRoute.id}/assign`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(payload),
            })
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            toast.success("Исполнитель назначен")
            setAssignModalOpen(false)
            await fetchRoutes()
        } catch (e) {
            toast.error(e instanceof Error ? e.message : "Не удалось назначить исполнителя")
        } finally {
            setAssigning(false)
        }
    }

    const hasRoutes = useMemo(() => routes.length > 0, [routes])

    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold leading-none tracking-tight">Маршруты</h1>
                    <p className="text-sm text-muted-foreground">Планируемые рейсы по заполненным точкам.</p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={fetchRoutes} disabled={loading || generating}>
                        <RefreshCw className="mr-2 h-4 w-4"/>
                        Обновить
                    </Button>
                    <Button size="sm" onClick={handleGenerate} disabled={loading || generating}>
                        {generating && <Loader2 className="mr-2 h-4 w-4 animate-spin"/>}
                        Создать маршрут
                    </Button>
                </div>
            </div>

            {loading ? (
                <div className="flex h-40 items-center justify-center text-muted-foreground">
                    <Loader2 className="mr-2 h-5 w-5 animate-spin"/>
                    Загрузка маршрутов...
                </div>
            ) : !hasRoutes ? (
                <Card>
                    <CardContent className="py-8 text-center text-muted-foreground">
                        Маршруты пока не созданы.
                    </CardContent>
                </Card>
            ) : (
                <RoutesTable routes={routes} onDelete={deleting ? undefined : handleDelete} onAssign={openAssignModal}/>
            )}

            <Dialog open={assignModalOpen} onOpenChange={setAssignModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Назначить исполнителя для маршрута #{assignRoute?.id}</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-3">
                        <div className="space-y-2">
                            <Label>Водитель</Label>
                            <Popover
                                open={isDriverPopoverOpen}
                                onOpenChange={(open) => {
                                    setIsDriverPopoverOpen(open)
                                    if (open) fetchDrivers()
                                }}
                            >
                                <PopoverTrigger asChild>
                                    <Button variant="outline" role="combobox" className="w-full justify-between">
                                        {selectedDriverLabel || "Выберите водителя"}
                                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50"/>
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent className="w-[420px] p-0">
                                    <Command>
                                        <CommandInput placeholder="Найти водителя..."/>
                                        <CommandEmpty>{isDriverLoading ? "Загрузка..." : "Ничего не найдено"}</CommandEmpty>
                                        <CommandGroup>
                                            {driverOptions.map((d) => (
                                                <CommandItem
                                                    key={d.id}
                                                    value={`${d.name || ""} ${d.phone || ""}`.trim() || `#${d.id}`}
                                                    onSelect={() => {
                                                        setSelectedDriverId(d.id)
                                                        setSelectedDriverLabel(`${d.name || "Без имени"} (#${d.id})`)
                                                        setIsDriverPopoverOpen(false)
                                                    }}
                                                >
                                                    <span className="mr-2 text-muted-foreground">#{d.id}</span>
                                                    <span>{d.name || "(без имени)"}</span>
                                                    {d.phone && (
                                                        <span className="ml-2 text-xs text-muted-foreground">
                                                            {d.phone}
                                                        </span>
                                                    )}
                                                </CommandItem>
                                            ))}
                                        </CommandGroup>
                                    </Command>
                                </PopoverContent>
                            </Popover>
                        </div>
                        <div className="space-y-2">
                            <Label>Плановое начало</Label>
                            <div className="flex gap-3">
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" className="flex-1 justify-between font-normal">
                                            {plannedStartDate || "Выберите дату"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 opacity-50"/>
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-3">
                                        <Input
                                            type="date"
                                            value={plannedStartDate}
                                            onChange={(e) => setPlannedStartDate(e.target.value)}
                                        />
                                    </PopoverContent>
                                </Popover>
                                <Input
                                    type="time"
                                    step="60"
                                    value={plannedStartTime}
                                    onChange={(e) => setPlannedStartTime(e.target.value)}
                                    className="w-32"
                                />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <Label>Плановое окончание</Label>
                            <div className="flex gap-3">
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <Button variant="outline" className="flex-1 justify-between font-normal">
                                            {plannedEndDate || "Выберите дату"}
                                            <ChevronsUpDown className="ml-2 h-4 w-4 opacity-50"/>
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-3">
                                        <Input
                                            type="date"
                                            value={plannedEndDate}
                                            onChange={(e) => setPlannedEndDate(e.target.value)}
                                        />
                                    </PopoverContent>
                                </Popover>
                                <Input
                                    type="time"
                                    step="60"
                                    value={plannedEndTime}
                                    onChange={(e) => setPlannedEndTime(e.target.value)}
                                    className="w-32"
                                />
                            </div>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setAssignModalOpen(false)}>
                            Отмена
                        </Button>
                        <Button onClick={handleAssign} disabled={assigning}>
                            {assigning && <Loader2 className="mr-2 h-4 w-4 animate-spin"/>}
                            Сохранить
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}
