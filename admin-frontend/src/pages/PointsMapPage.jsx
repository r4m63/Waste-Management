// src/pages/PointsMapPage.jsx

import {useCallback, useEffect, useMemo, useState} from "react"
import {Map, Placemark, YMaps} from "@pbe/react-yandex-maps"

import {Button} from "@/components/ui/button"
import {ChevronsUpDown, MapPin, Plus} from "lucide-react"
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
import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover"
import {Command, CommandEmpty, CommandGroup, CommandInput, CommandItem,} from "@/components/ui/command"
import {toast} from "sonner"
import {API_BASE} from "../../cfg.js"

export default function PointsMapPage() {
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [activePoint, setActivePoint] = useState(null)

    // поля формы
    const [address, setAddress] = useState("")
    const [capacity, setCapacity] = useState("")
    const [isOpen, setIsOpen] = useState(true)
    const [lat, setLat] = useState("")
    const [lon, setLon] = useState("")
    const [kioskId, setKioskId] = useState(null)

    // точки для карты
    const [points, setPoints] = useState([])
    const [isPointsLoading, setIsPointsLoading] = useState(false)

    // combobox киосков
    const [kioskOptions, setKioskOptions] = useState([])
    const [isKioskLoading, setIsKioskLoading] = useState(false)
    const [isKioskPopoverOpen, setIsKioskPopoverOpen] = useState(false)

    // карта
    const [mapCenter, setMapCenter] = useState([59.93, 30.31]) // СПб по дефолту
    const [mapZoom, setMapZoom] = useState(11)

    const resetForm = () => {
        setActivePoint(null)
        setAddress("")
        setCapacity("")
        setIsOpen(true)
        setLat("")
        setLon("")
        setKioskId(null)
    }

    // ================== Загрузка точек для карты ==================

    const fetchPoints = useCallback(async () => {
        setIsPointsLoading(true)
        try {
            // Тянем точки через /query — первые 1000 штук
            const body = {
                startRow: 0,
                endRow: 1000,
                sortModel: [],
                filterModel: {},
            }

            const res = await fetch(`${API_BASE}/api/garbage-points/query`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            })

            if (!res.ok) {
                console.error("Не удалось получить точки для карты", res.status, res.statusText)
                return
            }

            const data = await res.json() // { rows, lastRow }
            setPoints(data.rows || [])
        } catch (e) {
            console.error("Ошибка загрузки точек для карты", e)
        } finally {
            setIsPointsLoading(false)
        }
    }, [])

    useEffect(() => {
        fetchPoints()
    }, [fetchPoints])

    // ================== Загрузка киосков для combobox ==================

    const fetchKiosks = useCallback(async () => {
        // если уже загружали — не дёргаем бэк ещё раз
        if (kioskOptions.length > 0 || isKioskLoading) return;

        setIsKioskLoading(true);
        try {
            const body = {
                startRow: 0,
                endRow: 50,
                sortModel: [{colId: "createdAt", sort: "desc"}],
                // ❌ filterModel по role не нужен — репозиторий и так режет только KIOSK
                // filterModel: {
                //   role: { filterType: "text", type: "equals", filter: "KIOSK" },
                // },
                filterModel: {}, // или вообще не слать это поле
            };

            const res = await fetch(`${API_BASE}/api/kiosk/query`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const text = await res.text().catch(() => "");
                console.error("Не удалось получить список киосков", res.status, res.statusText, text);
                return;
            }

            const data = await res.json();
            console.log("kiosk query result:", data);
            // ожидаем data.rows: [{id,name,login,active,createdAt,...}]
            setKioskOptions(data.rows || []);
        } catch (e) {
            console.error("Ошибка загрузки киосков", e);
        } finally {
            setIsKioskLoading(false);
        }
    }, [kioskOptions.length, isKioskLoading]);


    useEffect(() => {
        if (isDialogOpen) {
            fetchKiosks()
        }
    }, [isDialogOpen, fetchKiosks])

    const selectedKioskLabel = useMemo(() => {
        if (kioskId == null) return "";
        const found = kioskOptions.find((k) => k.id === kioskId || k.id === Number(kioskId),);
        if (!found) return `ID ${kioskId}`;
        const name = found.name || "(без имени)";
        return found.login ? `${name} (${found.login})` : name;
    }, [kioskId, kioskOptions]);

    // ================== Валидация и сохранение ==================

    const validate = () => {
        if (!address.trim()) return "Заполните адрес."
        if (!capacity || Number(capacity) <= 0) return "Вместимость должна быть > 0."
        if (lat === "" || lon === "") return "Выберите координаты на карте."
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
            address: address.trim(),
            capacity: capacity === "" ? null : Number(capacity),
            open: isOpen,
            lat: lat === "" ? null : Number(lat),
            lon: lon === "" ? null : Number(lon),
            kioskId: kioskId ?? null,
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
                await fetchPoints()
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

    // ================== Работа с картой ==================

    // клик по карте — ставим координаты (для создания / редактирования)
    const handleMapClick = useCallback(
        (e) => {
            const coords = e.get("coords") // [lat, lon]
            if (!coords) return

            const [latVal, lonVal] = coords
            setLat(latVal.toFixed(6))
            setLon(lonVal.toFixed(6))

            // если сейчас ничего не редактируем — открываем модалку создания
            if (!activePoint) {
                setIsOpen(true)
                setCapacity("")
                setAddress("")
                setKioskId(null)
                setIsDialogOpen(true)
            }
        },
        [activePoint],
    )

    // клик по маркеру — редактирование существующей точки
    const handlePlacemarkClick = useCallback((point) => {
        setActivePoint(point)
        setAddress(point.address ?? "")
        setCapacity(point.capacity != null ? String(point.capacity) : "")
        setIsOpen(point.open ?? true)
        setLat(point.lat != null ? String(point.lat) : "")
        setLon(point.lon != null ? String(point.lon) : "")
        // kioskId — подстраиваемся к тому, как выглядет dto для rows
        setKioskId(
            point.kioskId ??
            point.kiosk_id ??
            point.kiosk?.id ??
            null,
        )

        if (point.lat && point.lon) {
            setMapCenter([point.lat, point.lon])
            setMapZoom(14)
        }

        setIsDialogOpen(true)
    }, [])

    const ymapsQuery = useMemo(
        () => ({
            apikey: "YOUR_YANDEX_MAPS_API_KEY", // TODO: подставь свой ключ
            lang: "ru_RU",
        }),
        [],
    )

    return (
        <>
            <div className="flex flex-1 min-h-0 flex-col gap-4">
                <div className="flex items-center justify-between gap-4">
                    <div className="flex flex-col gap-1">
                        <h1 className="text-2xl font-semibold leading-none tracking-tight">
                            Карта точек сбора
                        </h1>
                        <p className="text-sm text-muted-foreground">
                            Просмотр и редактирование точек сбора мусора прямо на карте.
                        </p>
                    </div>

                    <Button
                        size="sm"
                        className="gap-2"
                        onClick={() => {
                            resetForm()
                            // Центрируемся на дефолт / последней точке
                            setIsDialogOpen(true)
                        }}
                    >
                        <Plus className="h-4 w-4"/>
                        Новая точка
                    </Button>
                </div>

                <div className="flex-1 min-h-[400px] rounded-lg border bg-card overflow-hidden">
                    <YMaps query={ymapsQuery}>
                        <Map
                            defaultState={{center: mapCenter, zoom: mapZoom}}
                            state={{center: mapCenter, zoom: mapZoom}}
                            onBoundsChange={(e) => {
                                const newCenter = e.get("newCenter")
                                const newZoom = e.get("newZoom")
                                if (newCenter) setMapCenter(newCenter)
                                if (newZoom) setMapZoom(newZoom)
                            }}
                            onClick={handleMapClick}
                            width="100%"
                            height="100%"
                            options={{
                                suppressMapOpenBlock: true,
                            }}
                        >
                            {/* существующие точки */}
                            {points.map((p) =>
                                p.lat != null && p.lon != null ? (
                                    <Placemark
                                        key={p.id}
                                        geometry={[p.lat, p.lon]}
                                        onClick={() => handlePlacemarkClick(p)}
                                        options={{
                                            preset: "islands#greenDotIcon",
                                        }}
                                        properties={{
                                            balloonContentHeader: p.address || "Точка",
                                            balloonContentBody: `
                                                Вместимость: ${p.capacity ?? "-"}<br/>
                                                Открыта: ${p.open ? "Да" : "Нет"}
                                            `,
                                        }}
                                    />
                                ) : null,
                            )}

                            {/* текущая редактируемая/создаваемая точка (если lat/lon заданы) */}
                            {lat !== "" && lon !== "" && (
                                <Placemark
                                    geometry={[Number(lat), Number(lon)]}
                                    options={{
                                        preset: "islands#redDotIconWithCaption",
                                        draggable: true,
                                    }}
                                    properties={{
                                        iconCaption: "Выбранная точка",
                                    }}
                                    onDragEnd={(e) => {
                                        const coords = e.get("target").geometry.getCoordinates()
                                        if (!coords) return
                                        const [latVal, lonVal] = coords
                                        setLat(latVal.toFixed(6))
                                        setLon(lonVal.toFixed(6))
                                    }}
                                />
                            )}
                        </Map>
                    </YMaps>
                </div>
            </div>

            {/* Модалка создания/редактирования точки */}
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {activePoint ? "Редактирование точки" : "Новая точка сбора"}
                        </DialogTitle>
                        <DialogDescription className="flex items-center gap-2">
                            Кликните по карте, чтобы выбрать координаты точки.
                            <MapPin className="h-4 w-4 text-muted-foreground"/>
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
                                            <CommandInput placeholder="Найти киоск..."/>
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
                                                            setKioskId(k.id);      // сохраняем ID
                                                            setIsKioskPopoverOpen(false);
                                                        }}
                                                    >
                                                        <span className="mr-2 text-muted-foreground">
                                                            #{k.id}
                                                        </span>
                                                        <span>{k.name || "(без имени)"}</span>
                                                        {k.login && (
                                                            <span className="ml-2 text-xs text-muted-foreground">
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
