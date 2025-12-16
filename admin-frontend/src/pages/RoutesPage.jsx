// pages/RoutesPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Button} from "@/components/ui/button"
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card"
import {API_BASE} from "../../cfg.js"
import {apiFetch} from "@/lib/apiClient.js"
import {toast} from "sonner"
import {Loader2, MapPin, RefreshCw, Trash} from "lucide-react"

export default function RoutesPage() {
    const [routes, setRoutes] = useState([])
    const [loading, setLoading] = useState(false)
    const [generating, setGenerating] = useState(false)

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

    useEffect(() => {
        fetchRoutes()
    }, [fetchRoutes])

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
                <div className="grid gap-4 md:grid-cols-2">
                    {routes.map((route) => (
                        <Card key={route.id} className="flex flex-col">
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Trash className="h-5 w-5 text-muted-foreground"/>
                                    Маршрут #{route.id}
                                    <span className="text-sm font-normal text-muted-foreground">
                                        {route.plannedDate || "—"}
                                    </span>
                                    <span className="rounded-full bg-muted px-2 py-0.5 text-xs capitalize">
                                        {route.status || "planned"}
                                    </span>
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="flex flex-col gap-3">
                                <div className="text-sm text-muted-foreground">
                                    Остановок: {route.stops?.length || 0}
                                </div>
                                <div className="space-y-2">
                                    {(route.stops || []).map((stop) => (
                                        <div
                                            key={stop.id || `${route.id}-${stop.seqNo}`}
                                            className="rounded-lg border border-muted bg-muted/30 p-3"
                                        >
                                            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                                                <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary">
                                                    #{stop.seqNo}
                                                </span>
                                                <span className="capitalize">{stop.status || "planned"}</span>
                                            </div>
                                            <div className="mt-1 flex items-start gap-2">
                                                <MapPin className="h-4 w-4 text-muted-foreground mt-0.5"/>
                                                <div>
                                                    <div className="font-medium">
                                                        {stop.address || `Точка #${stop.garbagePointId}`}
                                                    </div>
                                                    {stop.expectedCapacity != null && (
                                                        <div className="text-sm text-muted-foreground">
                                                            Ожидаемый объём: {stop.expectedCapacity}
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    )
}
