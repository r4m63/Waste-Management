import {
    Card,
    CardBody,
    Chip,
} from '@nextui-org/react'

const containerSizes = [
    {
        id: 'xs',
        label: 'XS',
        width: 40,
        length: 30,
        height: 30,
        description: 'Малые пакеты, до 15 кг',
    },
    {
        id: 's',
        label: 'S',
        width: 60,
        length: 40,
        height: 40,
        description: 'Небольшие мешки и коробки, до 25 кг',
    },
    {
        id: 'm',
        label: 'M',
        width: 80,
        length: 60,
        height: 60,
        description: 'Стандартные мешки, до 40 кг',
    },
    {
        id: 'l',
        label: 'L',
        width: 100,
        length: 70,
        height: 70,
        description: 'Крупные мешки или контейнеры, до 60 кг',
    },
    {
        id: 'xl',
        label: 'XL',
        width: 120,
        length: 90,
        height: 90,
        description: 'Паллетные короба, до 90 кг',
    },
    {
        id: 'xxl',
        label: 'XXL',
        width: 140,
        length: 100,
        height: 100,
        description: 'Габаритные контейнеры, до 120 кг',
    },
    {
        id: 'xxxl',
        label: 'XXXL',
        width: 160,
        length: 120,
        height: 120,
        description: 'Максимальный объём, до 200 кг',
    },
]

export default function ContainerStep({containerSize, onSelectSize}) {
    return (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {containerSizes.map((size) => {
                const isSelected = containerSize === size.id
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
                                <h3 className="text-3xl font-semibold">{size.label}</h3>
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
                                            {size.width} см
                                        </span>
                                    </p>
                                    <p>
                                        Длина:{' '}
                                        <span className="font-semibold text-foreground">
                                            {size.length} см
                                        </span>
                                    </p>
                                    <p>
                                        Высота:{' '}
                                        <span className="font-semibold text-foreground">
                                            {size.height} см
                                        </span>
                                    </p>
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
