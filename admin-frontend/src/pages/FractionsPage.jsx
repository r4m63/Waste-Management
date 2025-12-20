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
import ContainerSizesTable from "@/components/tableData/ContainerSizesTable.jsx";
import {apiFetch} from "@/lib/apiClient.js";

export default function FractionsPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [activeFraction, setActiveFraction] = useState(null);
    const [isSizeDialogOpen, setIsSizeDialogOpen] = useState(false);
    const [activeSize, setActiveSize] = useState(null);

    const [name, setName] = useState("");
    const [code, setCode] = useState("");
    const [description, setDescription] = useState("");
    const [hazardous, setHazardous] = useState(false);

    const [refreshGrid, setRefreshGrid] = useState(() => () => {
    });
    const [refreshSizesGrid, setRefreshSizesGrid] = useState(() => () => {});

    const [codeSize, setCodeSize] = useState("");
    const [capacity, setCapacity] = useState("");
    const [length, setLength] = useState("");
    const [width, setWidth] = useState("");
    const [height, setHeight] = useState("");
    const [descriptionSize, setDescriptionSize] = useState("");

    const resetForm = () => {
        setActiveFraction(null);
        setName("");
        setCode("");
        setDescription("");
        setHazardous(false);
    };

    const resetSizeForm = () => {
        setActiveSize(null);
        setCodeSize("");
        setCapacity("");
        setLength("");
        setWidth("");
        setHeight("");
        setDescriptionSize("");
    };

    const validate = () => {
        if (!name.trim()) return "Укажите название фракции.";
        if (!code.trim()) return "Укажите код фракции.";
        return null;
    };

    const validateSize = () => {
        if (!codeSize.trim()) return "Укажите код контейнера.";
        if (capacity === "" || Number(capacity) <= 0) return "Вместимость должна быть > 0.";
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
            const res = await apiFetch(url, {
                method: isEdit ? "PUT" : "POST",
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

    const handleSaveSize = async () => {
        const err = validateSize();
        if (err) {
            toast.warning(err);
            return;
        }

        const payload = {
            code: codeSize.trim(),
            capacity: Number(capacity),
            length: length === "" ? null : Number(length),
            width: width === "" ? null : Number(width),
            height: height === "" ? null : Number(height),
            description: descriptionSize.trim() || null,
        };

        const isEdit = Boolean(activeSize?.id);
        const url = isEdit
            ? `${API_BASE}/api/container-sizes/${activeSize.id}`
            : `${API_BASE}/api/container-sizes`;

        try {
            const res = await apiFetch(url, {
                method: isEdit ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(payload),
            });

            if (res.ok) {
                refreshSizesGrid?.();
                setIsSizeDialogOpen(false);
                resetSizeForm();
                toast.success("Сохранено");
            } else {
                const errorData = await res.json().catch(() => ({}));
                toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`);
            }
        } catch (e) {
            console.error("Ошибка сохранения размера контейнера", e);
            toast.error("Ошибка сохранения. Попробуйте ещё раз.");
        }
    };

    const handleCancelSize = () => {
        setIsSizeDialogOpen(false);
        resetSizeForm();
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
                const res = await apiFetch(`${API_BASE}/api/fractions/${row.id}`, {
                    method: "DELETE",
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

    const handleOpenEditSizeModal = useCallback((row) => {
        setActiveSize(row);
        setCodeSize(row?.code ?? "");
        setCapacity(row?.capacity != null ? String(row.capacity) : "");
        setLength(row?.length != null ? String(row.length) : "");
        setWidth(row?.width != null ? String(row.width) : "");
        setHeight(row?.height != null ? String(row.height) : "");
        setDescriptionSize(row?.description ?? "");
        setIsSizeDialogOpen(true);
    }, []);

    const handleDeleteSize = useCallback(
        async (row) => {
            if (!row?.id) return;
            if (!window.confirm(`Удалить размер #${row.id}?`)) return;

            try {
                const res = await apiFetch(`${API_BASE}/api/container-sizes/${row.id}`, {
                    method: "DELETE",
                });

                if (res.ok) {
                    toast.success("Удалено");
                    refreshSizesGrid?.();
                } else {
                    const errorData = await res.json().catch(() => ({}));
                    toast.error(errorData.message || `Ошибка: ${res.status} ${res.statusText}`);
                }
            } catch (e) {
                console.error("Ошибка удаления размера", e);
                toast.error("Ошибка удаления. Попробуйте ещё раз.");
            }
        },
        [refreshSizesGrid]
    );

    return (
        <>
            <div className="flex flex-1 min-h-0 flex-col gap-6">
                <section className="flex flex-col gap-3">
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

                    <div className="flex-1 min-h-[320px]">
                        <FractionsTable
                            onOpenEditFractionModal={handleOpenEditFractionModal}
                            onDeleteFraction={handleDeleteFraction}
                            onReadyRefresh={(fn) => setRefreshGrid(() => fn)}
                        />
                    </div>
                </section>

                <section className="flex flex-col gap-3">
                    <div className="flex items-center justify-between gap-4">
                        <div className="flex flex-col gap-1">
                            <h2 className="text-xl font-semibold leading-none tracking-tight">
                                Размеры контейнеров
                            </h2>
                            <p className="text-sm text-muted-foreground">
                                Справочник размеров и габаритов.
                            </p>
                        </div>

                        <Button
                            size="sm"
                            className="gap-2"
                            onClick={() => {
                                resetSizeForm();
                                setIsSizeDialogOpen(true);
                            }}
                        >
                            <Plus className="h-4 w-4"/>
                            Добавить размер
                        </Button>
                    </div>

                    <div className="flex-1 min-h-[320px]">
                        <ContainerSizesTable
                            onOpenEditSizeModal={handleOpenEditSizeModal}
                            onDeleteSize={handleDeleteSize}
                            onReadyRefresh={(fn) => setRefreshSizesGrid(() => fn)}
                        />
                    </div>
                </section>
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

            <Dialog open={isSizeDialogOpen} onOpenChange={setIsSizeDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activeSize ? "Редактирование размера" : "Новый размер контейнера"}
                        </DialogTitle>
                        <DialogDescription>
                            Заполните код, вместимость и габариты контейнера.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4">
                        <div className="grid gap-4">
                            <div className="space-y-2">
                                <Label htmlFor="codeSize">Код</Label>
                                <Input
                                    id="codeSize"
                                    placeholder="XS, S, M..."
                                    value={codeSize}
                                    onChange={(e) => setCodeSize(e.target.value)}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="capacity">Вместимость</Label>
                                <Input
                                    id="capacity"
                                    type="number"
                                    min={1}
                                    placeholder="Например, 10"
                                    value={capacity}
                                    onChange={(e) => setCapacity(e.target.value)}
                                />
                            </div>

                            <div className="grid grid-cols-3 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="length">Длина</Label>
                                    <Input
                                        id="length"
                                        type="number"
                                        placeholder="м"
                                        value={length}
                                        onChange={(e) => setLength(e.target.value)}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="width">Ширина</Label>
                                    <Input
                                        id="width"
                                        type="number"
                                        placeholder="м"
                                        value={width}
                                        onChange={(e) => setWidth(e.target.value)}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="height">Высота</Label>
                                    <Input
                                        id="height"
                                        type="number"
                                        placeholder="м"
                                        value={height}
                                        onChange={(e) => setHeight(e.target.value)}
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="descriptionSize">Описание</Label>
                                <Input
                                    id="descriptionSize"
                                    placeholder="Описание размера"
                                    value={descriptionSize}
                                    onChange={(e) => setDescriptionSize(e.target.value)}
                                />
                            </div>
                        </div>
                    </div>

                    <DialogFooter className="mt-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleCancelSize}
                        >
                            Отмена
                        </Button>
                        <Button type="button" onClick={handleSaveSize}>
                            Сохранить
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </>
    );
}
