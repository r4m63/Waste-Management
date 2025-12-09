// pages/PosTerminalsPage.jsx

import {useCallback, useState} from "react"
import {Button} from "@/components/ui/button"
import {Plus} from "lucide-react"
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
import PosTerminalsTable from "@/components/tableData/PosTerminalsTable.jsx"
import {API_BASE} from "../../cfg.js"
import {toast} from "sonner"
import {parseApiError} from "@/lib/utils.js"

export default function PosTerminalsPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [activeTerminal, setActiveTerminal] = useState(null)

    const [name, setName] = useState("")
    const [login, setLogin] = useState("")
    const [password, setPassword] = useState("")
    const [isActive, setIsActive] = useState(true)

    const [refreshGrid, setRefreshGrid] = useState(() => () => {})
    const [tableControls, setTableControls] = useState(null)

    const resetForm = () => {
        setActiveTerminal(null)
        setName("")
        setLogin("")
        setPassword("")
        setIsActive(true)
    }

    const validate = () => {
        if (!name.trim()) return "Укажите имя терминала."
        if (!login.trim()) return "Укажите логин."
        if (!activeTerminal && !password.trim()) return "Укажите пароль для нового терминала."
        return null
    }

    const handleSave = async () => {
        const err = validate()
        if (err) {
            toast.warning(err)
            return
        }

        const payload = {
            id: activeTerminal?.id ?? null,
            name: name.trim(),
            login: login.trim(),
            password: password.trim() || null,
            active: isActive,
        }

        const isEdit = Boolean(activeTerminal?.id)
        const url = isEdit
            ? `${API_BASE}/api/kiosk/${activeTerminal.id}`
            : `${API_BASE}/api/kiosk`

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
                const errorMessage = await parseApiError(res, "Ошибка сохранения")
                toast.error(errorMessage)
            }
        } catch (e) {
            console.error("Ошибка сохранения терминала", e)
            toast.error("Ошибка сети. Попробуйте ещё раз.")
        }
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm()
    }

    const handleOpenEditTerminalModal = useCallback((row) => {
        setActiveTerminal(row)
        setName(row?.name ?? "")
        setLogin(row?.login ?? "")
        setPassword("")
        setIsActive(row?.active ?? true)
        setIsDialogOpen(true)
    }, [])

    // 🔥 УДАЛЕНИЕ
    const handleDeleteTerminal = useCallback(
        async (row) => {
            if (!row?.id) return

            if (!window.confirm(`Удалить терминал #${row.id}?`)) return

            try {
                const res = await fetch(`${API_BASE}/api/kiosk/${row.id}`, {
                    method: "DELETE",
                    credentials: "include",
                })

                if (res.ok) {
                    toast.success("Терминал удалён")
                    refreshGrid?.()
                } else {
                    const errorMessage = await parseApiError(res, "Ошибка удаления")
                    toast.error(errorMessage)
                }
            } catch (e) {
                console.error("Ошибка удаления терминала", e)
                toast.error("Ошибка сети. Попробуйте ещё раз.")
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
                            POS-терминалы
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Управление терминалами и статусами.
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
                        Добавить терминал
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <PosTerminalsTable
                        onOpenEditTerminalModal={handleOpenEditTerminalModal}
                        onDeleteTerminal={handleDeleteTerminal}   // 👈 ПРОКИНУЛИ
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        onReadyControls={(controls) => setTableControls(controls)}
                    />
                </div>
            </div>

            {/* Dialog создания/редактирования — как было */}
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activeTerminal ? "Редактирование терминала" : "Новый терминал"}
                        </DialogTitle>
                        <DialogDescription>
                            Заполните данные POS-терминала (системная учётная запись).
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="name">Имя терминала</Label>
                                <Input
                                    id="name"
                                    placeholder="POS-киоск #1"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="login">Логин</Label>
                                <Input
                                    id="login"
                                    placeholder="pos_kiosk_01"
                                    value={login}
                                    onChange={(e) => setLogin(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="password">
                                    Пароль {activeTerminal && "(оставьте пустым, чтобы не менять)"}
                                </Label>
                                <Input
                                    id="password"
                                    type="password"
                                    placeholder="••••••••"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                            </div>

                            <div className="flex items-center gap-3 pt-2">
                                <Switch
                                    id="isActive"
                                    checked={isActive}
                                    onCheckedChange={(checked) => setIsActive(checked)}
                                />
                                <Label htmlFor="isActive" className="cursor-pointer">
                                    Активен
                                </Label>
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
