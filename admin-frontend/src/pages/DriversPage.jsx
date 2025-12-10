// src/pages/DriversPage.jsx

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
import {toast} from "sonner"
import {API_BASE} from "../../cfg.js"
import DriversTable from "@/components/tableData/DriversTable.jsx"

export default function DriversPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [activeDriver, setActiveDriver] = useState(null)

    const [name, setName] = useState("")
    const [phone, setPhone] = useState("")
    const [login, setLogin] = useState("")
    const [password, setPassword] = useState("")
    const [isActive, setIsActive] = useState(true)

    const [refreshGrid, setRefreshGrid] = useState(() => () => {
    })
    const [tableControls, setTableControls] = useState(null)

    const resetForm = () => {
        setActiveDriver(null)
        setName("")
        setPhone("")
        setLogin("")
        setPassword("")
        setIsActive(true)
    }

    const validate = () => {
        if (!name.trim()) return "Укажите имя водителя."
        if (!phone.trim()) return "Укажите телефон."
        if (!login.trim()) return "Укажите логин."
        if (!activeDriver && !password.trim()) {
            return "Укажите пароль для нового водителя."
        }
        return null
    }

    const handleSave = async () => {
        const err = validate()
        if (err) {
            toast.warning(err)
            return
        }

        const payload = {
            name: name.trim(),
            phone: phone.trim(),
            login: login.trim(),
            password: password.trim() || null,
            active: isActive,
        }

        const isEdit = Boolean(activeDriver?.id)
        const url = isEdit
            ? `${API_BASE}/api/drivers/${activeDriver.id}`
            : `${API_BASE}/api/drivers`

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
            console.error("Ошибка сохранения водителя", e)
            toast.error("Ошибка сохранения. Попробуйте ещё раз.")
        }
    }

    const handleCancel = () => {
        setIsDialogOpen(false)
        resetForm()
    }

    const handleOpenEditDriverModal = useCallback((row) => {
        setActiveDriver(row)
        setName(row?.name ?? "")
        setPhone(row?.phone ?? "")
        setLogin(row?.login ?? "")
        setPassword("")
        setIsActive(row?.active ?? true)
        setIsDialogOpen(true)
    }, [])

    const handleDeleteDriver = useCallback(
        async (row) => {
            if (!row?.id) return
            if (!window.confirm(`Удалить водителя #${row.id}?`)) return

            try {
                const res = await fetch(`${API_BASE}/api/drivers/${row.id}`, {
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
                console.error("Ошибка удаления водителя", e)
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
                            Водители
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Список водителей (пользователи с ролью DRIVER).
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
                        <Plus className="h-4 w-4"/>
                        Добавить водителя
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <DriversTable
                        onOpenEditDriverModal={handleOpenEditDriverModal}
                        onDeleteDriver={handleDeleteDriver}
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        onReadyControls={(controls) => setTableControls(controls)}
                    />
                </div>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activeDriver ? "Редактирование водителя" : "Новый водитель"}
                        </DialogTitle>
                        <DialogDescription>
                            Заполните данные водителя, они будут сохранены в системе.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="name">Имя</Label>
                                <Input
                                    id="name"
                                    placeholder="Иван Иванов"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="phone">Телефон</Label>
                                <Input
                                    id="phone"
                                    placeholder="+7 999 000-00-00"
                                    value={phone}
                                    onChange={(e) => setPhone(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="login">Логин</Label>
                                <Input
                                    id="login"
                                    placeholder="driver_01"
                                    value={login}
                                    onChange={(e) => setLogin(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="password">
                                    Пароль {activeDriver && "(оставьте пустым, чтобы не менять)"}
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
