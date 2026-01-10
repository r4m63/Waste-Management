// pages/ShiftsPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Button} from "@/components/ui/button"
import {Card, CardContent} from "@/components/ui/card"
import {API_BASE} from "../../cfg.js"
import {apiFetch} from "@/lib/apiClient.js"
import {toast} from "sonner"
import {Loader2, RefreshCw, Clock} from "lucide-react"
import ShiftsTable from "@/components/tableData/ShiftsTable.jsx"

export default function ShiftsPage() {
    const [shifts, setShifts] = useState([])
    const [loading, setLoading] = useState(false)

    const fetchShifts = useCallback(async () => {
        setLoading(true)
        try {
            const res = await apiFetch(`${API_BASE}/api/shifts`)
            if (!res.ok) {
                const text = await res.text().catch(() => "")
                throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
            }
            const data = await res.json()
            setShifts(Array.isArray(data) ? data : [])
        } catch (e) {
            console.error("Не удалось загрузить смены", e)
            toast.error(e.message || "Не удалось загрузить смены")
            setShifts([])
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        fetchShifts()
    }, [fetchShifts])

    const hasShifts = useMemo(() => shifts.length > 0, [shifts])
    const openCount = useMemo(() => shifts.filter(s => s.status === "open").length, [shifts])

    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold leading-none tracking-tight">Смены водителей</h1>
                    <p className="text-sm text-muted-foreground">
                        История открытия и закрытия смен.
                        {openCount > 0 && (
                            <span className="ml-2 inline-flex items-center gap-1 text-green-400">
                                <Clock className="h-3.5 w-3.5"/>
                                {openCount} активн{openCount === 1 ? "ая" : openCount < 5 ? "ых" : "ых"}
                            </span>
                        )}
                    </p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={fetchShifts} disabled={loading}>
                        <RefreshCw className="mr-2 h-4 w-4"/>
                        Обновить
                    </Button>
                </div>
            </div>

            {loading ? (
                <div className="flex h-40 items-center justify-center text-muted-foreground">
                    <Loader2 className="mr-2 h-5 w-5 animate-spin"/>
                    Загрузка смен...
                </div>
            ) : !hasShifts ? (
                <Card>
                    <CardContent className="py-8 text-center text-muted-foreground">
                        Смен пока нет.
                    </CardContent>
                </Card>
            ) : (
                <ShiftsTable shifts={shifts}/>
            )}
        </div>
    )
}

