// pages/IncidentsPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Button} from "@/components/ui/button"
import {Card, CardContent} from "@/components/ui/card"
import {API_BASE} from "../../cfg.js"
import {apiFetch} from "@/lib/apiClient.js"
import {toast} from "sonner"
import {Loader2, RefreshCw, AlertTriangle} from "lucide-react"
import IncidentsTable from "@/components/tableData/IncidentsTable.jsx"

export default function IncidentsPage() {
    const [incidents, setIncidents] = useState([])
    const [loading, setLoading] = useState(false)
    const [resolving, setResolving] = useState(false)

    const fetchIncidents = useCallback(async () => {
        setLoading(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/incidents`)
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            const data = await res.json()
            setIncidents(Array.isArray(data) ? data : [])
        } catch (e) {
            console.error("Не удалось загрузить инциденты", e)
            toast.error(e.message || "Не удалось загрузить инциденты")
            setIncidents([])
        } finally {
            setLoading(false)
        }
    }, [])

    const handleResolve = async (incident) => {
        if (!incident?.id) return
        if (!window.confirm(`Отметить инцидент #${incident.id} как решённый?`)) return
        setResolving(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/incidents/${incident.id}/resolve`, {
                method: "PUT",
            })
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            toast.success(`Инцидент #${incident.id} отмечен как решённый`)
            await fetchIncidents()
        } catch (e) {
            toast.error(e instanceof Error ? e.message : "Не удалось обновить инцидент")
        } finally {
            setResolving(false)
        }
    }

    useEffect(() => {
        fetchIncidents()
    }, [fetchIncidents])

    const hasIncidents = useMemo(() => incidents.length > 0, [incidents])
    const openCount = useMemo(() => incidents.filter(i => !i.resolved).length, [incidents])

    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold leading-none tracking-tight">Инциденты</h1>
                    <p className="text-sm text-muted-foreground">
                        Проблемы, зарегистрированные водителями на маршрутах.
                        {openCount > 0 && (
                            <span className="ml-2 inline-flex items-center gap-1 text-amber-400">
                                <AlertTriangle className="h-3.5 w-3.5"/>
                                {openCount} открыт{openCount === 1 ? "" : openCount < 5 ? "о" : "о"}
                            </span>
                        )}
                    </p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={fetchIncidents} disabled={loading || resolving}>
                        <RefreshCw className="mr-2 h-4 w-4"/>
                        Обновить
                    </Button>
                </div>
            </div>

            {loading ? (
                <div className="flex h-40 items-center justify-center text-muted-foreground">
                    <Loader2 className="mr-2 h-5 w-5 animate-spin"/>
                    Загрузка инцидентов...
                </div>
            ) : !hasIncidents ? (
                <Card>
                    <CardContent className="py-8 text-center text-muted-foreground">
                        Инцидентов пока нет.
                    </CardContent>
                </Card>
            ) : (
                <IncidentsTable incidents={incidents} onResolve={resolving ? undefined : handleResolve}/>
            )}
        </div>
    )
}

