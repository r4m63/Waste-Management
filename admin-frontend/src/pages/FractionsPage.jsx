import React, {useCallback, useState} from "react";
import {Button} from "@/components/ui/button";
import {Plus} from "lucide-react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/input";
import {Switch} from "@/components/ui/switch";
import {toast} from "sonner";
import {API_BASE} from "../../cfg.js";
import FractionsTable from "@/components/tableData/FractionsTable.jsx";

export default function FractionsPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [activeFraction, setActiveFraction] = useState(null);

    const [name, setName] = useState("");
    const [code, setCode] = useState("");
    const [description, setDescription] = useState("");
    const [hazardous, setHazardous] = useState(false);

    const [refreshGrid, setRefreshGrid] = useState(() => () => {
    });

    const resetForm = () => {
        setActiveFraction(null);
        setName("");
        setCode("");
        setDescription("");
        setHazardous(false);
    };

    const validate = () => {
        if (!name.trim()) return "Укажите название фракции.";
        if (!code.trim()) return "Укажите код фракции.";
        return null;
    };

    const handleSave = async () => {
        const err = validate();
        if (err) {
            toast.warning(err);
            return;
        }

        const payload = {
            name: name.trim(),
            code: code.trim(),
            description: description.trim(),
            hazardous,
        };

        const isEdit = Boolean(activeFraction?.id);
        const url = isEdit
            ? `${API_BASE}/api/fractions/${activeFraction.id}`
            : `${API_BASE}/api/fractions`;

        try {
            const res = await fetch(url, {
                method: isEdit ? "PUT" : "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(payload),
            });

            if (res.ok) {
                refreshGrid?.();
                setIsDialogOpen(false);
                resetForm();
                toast.success("Сохранено");
            } else {
                const errorData = await res.json().catch(() => ({}));
                toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`);
            }
        } catch (e) {
            console.error("Ошибка сохранения фракции", e);
            toast.error("Ошибка сохранения. Попробуйте ещё раз.");
        }
    };

    const handleCancel = () => {
        setIsDialogOpen(false);
        resetForm();
    };

    const handleOpenEditFractionModal = useCallback((row) => {
        setActiveFraction(row);
        setName(row?.name ?? "");
        setCode(row?.code ?? "");
        setDescription(row?.description ?? "");
        setHazardous(row?.hazardous ?? false);
        setIsDialogOpen(true);
    }, []);

    const handleDeleteFraction = useCallback(
        async (row) => {
            if (!row?.id) return;
            if (!window.confirm(`Удалить фракцию #${row.id}?`)) return;

            try {
                const res = await fetch(`${API_BASE}/api/fractions/${row.id}`, {
                    method: "DELETE",
                    credentials: "include",
                });

                if (res.ok) {
                    toast.success("Удалено");
                    refreshGrid?.();
                } else {
                    const errorData = await res.json().catch(() => ({}));
                    toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`);
                }
            } catch (e) {
                console.error("Ошибка удаления фракции", e);
                toast.error("Ошибка удаления. Попробуйте ещё раз.");
            }
        },
        [refreshGrid]
    );

    return (
        <>
            <div className="flex flex-1 min-h-0 flex-col gap-4">
                <div className="flex items-center justify-between gap-4">
                    <div className="flex flex-col gap-1">
                        <h1 className="text-2xl font-semibold leading-none tracking-tight">
                            Фракции
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Управление фракциями: название, код и описание.
                        </p>
                    </div>

                    <Button
                        size="sm"
                        className="gap-2"
                        onClick={() => {
                            resetForm();
                            setIsDialogOpen(true);
                        }}
                    >
                        <Plus className="h-4 w-4"/>
                        Добавить фракцию
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px]">
                    <FractionsTable
                        onOpenEditFractionModal={handleOpenEditFractionModal}
                        onDeleteFraction={handleDeleteFraction}
                        onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                    />
                </div>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activeFraction ? "Редактирование фракции" : "Новая фракция"}
                        </DialogTitle>
                        <DialogDescription>
                            Заполните параметры фракции, они будут сохранены в системе.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="name">Название</Label>
                                <Input
                                    id="name"
                                    placeholder="Например, Пластик"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="code">Код</Label>
                                <Input
                                    id="code"
                                    placeholder="ПЛАСТ"
                                    value={code}
                                    onChange={(e) => setCode(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="description">Описание</Label>
                                <Input
                                    id="description"
                                    placeholder="Описание фракции"
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                />
                            </div>

                            <div className="flex items-center gap-3 pt-2">
                                <Switch
                                    id="hazardous"
                                    checked={hazardous}
                                    onCheckedChange={(checked) => setHazardous(checked)}
                                />
                                <Label htmlFor="hazardous" className="cursor-pointer">
                                    Опасная
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
    );
}
