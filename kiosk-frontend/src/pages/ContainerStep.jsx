import {useEffect, useMemo, useState} from 'react'
import {
    Card,
    CardBody,
    Chip,
    Spinner,
} from '@nextui-org/react'
import {apiFetch} from "../lib/apiClient.js";
import {API_BASE} from "../../cfg.js";

export default function ContainerStep({containerSize, onSelectSize}) {
    const [containerSizes, setContainerSizes] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        let isMounted = true

        const fetchContainerSizes = async () => {
            setIsLoading(true)
            setError(null)

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
                    throw new Error(`Ошибка загрузки (${res.status})`)
                }

                const data = await res.json()
                if (!isMounted) return
                setContainerSizes(Array.isArray(data.rows) ? data.rows : [])
            } catch (e) {
                console.error("Не удалось получить размеры контейнеров", e)
                if (isMounted) setError("Не удалось загрузить размеры контейнеров")
            } finally {
                if (isMounted) setIsLoading(false)
            }
        }

        fetchContainerSizes()

        return () => {
            isMounted = false
        }
    }, [])

    const sizesToRender = useMemo(() => containerSizes, [containerSizes])

    if (isLoading) {
        return (
            <div className="flex h-40 items-center justify-center">
                <Spinner color="primary" label="Загрузка размеров..." />
            </div>
        )
    }

    if (error) {
        return (
            <div className="rounded-xl border border-danger-100 bg-danger-50/40 p-4 text-danger-600">
                {error}
            </div>
        )
    }

    if (!sizesToRender.length) {
        return (
            <div className="rounded-xl border border-default-200 bg-content2/40 p-4 text-default-600">
                Размеры контейнеров не найдены.
            </div>
        )
    }

    return (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {sizesToRender.map((size) => {
                const isSelected = containerSize === size.id
                const label = size.code || `ID ${size.id}`
                const dimension = (value) => (value == null ? '—' : value)
                return (
                    <Card
                        key={size.id}
                        isPressable
                        onPress={() => onSelectSize(size.id)}
                        className={`relative border-2 transition-all ${
                            isSelected ? 'border-primary shadow-large' : 'border-default-100 bg-content2/40'
                        }`}
                    >
                        <CardBody className="flex min-h-[190px] flex-col gap-3">
                            <div>
                                <p className="text-xs uppercase tracking-[0.25em] text-default-500">Размер</p>
                                <h3 className="text-3xl font-semibold">{label}</h3>
                                <p className="text-small text-default-500">{size.description}</p>
                            </div>

                            <div className="mt-1 space-y-1 text-sm text-default-500">
                                <p className="text-xs uppercase tracking-[0.25em] text-default-400">
                                    Габариты
                                </p>
                                <div className="space-y-0.5">
                                    <p>
                                        Ширина:{' '}
                                        <span className="font-semibold text-foreground">
                                            {dimension(size.width)} см
                                        </span>
                                    </p>
                                    <p>
                                        Длина:{' '}
                                        <span className="font-semibold text-foreground">
                                            {dimension(size.length)} см
                                        </span>
                                    </p>
                                    <p>
                                        Высота:{' '}
                                        <span className="font-semibold text-foreground">
                                            {dimension(size.height)} см
                                        </span>
                                    </p>
                                    {size.capacity != null && (
                                        <p>
                                            Вместимость:{' '}
                                            <span className="font-semibold text-foreground">
                                                {size.capacity} л
                                            </span>
                                        </p>
                                    )}
                                </div>
                            </div>

                            <div className="mt-auto flex flex-wrap gap-2">
                                {isSelected && (
                                    <Chip color="primary" variant="dot">
                                        выбран
                                    </Chip>
                                )}
                            </div>
                        </CardBody>
                    </Card>
                )
            })}
        </div>
    )
}
