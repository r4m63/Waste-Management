import {useEffect, useMemo, useState} from 'react'
import {
    Card,
    CardBody,
    Chip,
    Spinner,
} from '@nextui-org/react'
import {apiFetch} from "../lib/apiClient.js";
import {API_BASE} from "../../cfg.js";

export default function WasteStep({wasteType, onSelectWasteType}) {
    const [fractions, setFractions] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        let isMounted = true

        const fetchFractions = async () => {
            setIsLoading(true)
            setError(null)

            try {
                const body = {
                    startRow: 0,
                    endRow: 100,
                    sortModel: [],
                    filterModel: {},
                }

                const res = await apiFetch(`${API_BASE}/api/fractions/query`, {
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
                setFractions(Array.isArray(data.rows) ? data.rows : [])
            } catch (e) {
                console.error("Не удалось получить фракции", e)
                if (isMounted) setError("Не удалось загрузить типы отходов")
            } finally {
                if (isMounted) setIsLoading(false)
            }
        }

        fetchFractions()

        return () => {
            isMounted = false
        }
    }, [])

    const categoriesToRender = useMemo(() => fractions, [fractions])

    if (isLoading) {
        return (
            <div className="flex h-40 items-center justify-center">
                <Spinner color="primary" label="Загрузка типов отходов..." />
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

    if (!categoriesToRender.length) {
        return (
            <div className="rounded-xl border border-default-200 bg-content2/40 p-4 text-default-600">
                Типы отходов не найдены.
            </div>
        )
    }

    return (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {categoriesToRender.map((category) => {
                const isSelected = wasteType === category.id
                const label = category.name || `ID ${category.id}`
                return (
                    <Card
                        key={category.id}
                        isPressable
                        onPress={() => onSelectWasteType(category.id)}
                        className={`relative border-2 transition-all ${
                            isSelected ? 'border-primary shadow-large' : 'border-default-100 bg-content2/40'
                        }`}
                    >
                        <CardBody className="flex min-h-[170px] flex-col gap-4">
                            <div className="flex items-start justify-between gap-2">
                                <div>
                                    <p className="text-xs uppercase tracking-[0.25em] text-default-500">
                                        {category.code || 'Фракция'}
                                    </p>
                                    <h3 className="text-2xl font-semibold">{label}</h3>
                                    <p className="text-sm text-default-500">{category.description}</p>
                                </div>
                                {category.hazardous && (
                                    <Chip color="danger" size="sm" variant="flat">
                                        Опасный отход
                                    </Chip>
                                )}
                            </div>
                            <div className="mt-auto">
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
