import {
    Button,
    Card,
    CardBody,
    Chip,
    Divider,
    Popover,
    PopoverContent,
    PopoverTrigger,
} from '@nextui-org/react'

const wasteCategories = [
    {
        id: 'plastic',
        label: 'Пластик',
        description: 'Бутылки, канистры, упаковка',
        icon: '🧴',
        info: ['ПЭТ-бутылки', 'Канистры и бочки', 'Стретч-пленка и пакеты'],
    },
    {
        id: 'glass',
        label: 'Стекло',
        description: 'Бутылки, банки, стеклобой',
        icon: '🍾',
        info: ['Бесцветные бутылки', 'Зелёное стекло', 'Банки и стеклобой'],
    },
    {
        id: 'paper',
        label: 'Макулатура',
        description: 'Картон, бумага, журналы',
        icon: '📦',
        info: ['Картонные коробки', 'Офисная бумага', 'Газеты и журналы'],
    },
    {
        id: 'metal',
        label: 'Металл',
        description: 'Банки, крышки, проволока',
        icon: '⚙️',
        info: ['Алюминиевые банки', 'Жесть и крышки', 'Цветной металл'],
    },
    {
        id: 'mix',
        label: 'MIX',
        description: 'Смешанная фракция без сортировки',
        icon: '♻️',
        info: ['Комбинированные материалы', 'Смешанные отходы', 'Небольшие партии без сортировки'],
    },
]

export default function WasteStep({wasteType, onSelectWasteType}) {
    return (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {wasteCategories.map((category) => {
                const isSelected = wasteType === category.id
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
                                    <span className="text-3xl">{category.icon}</span>
                                    <h3 className="text-2xl font-semibold">{category.label}</h3>
                                    <p className="text-sm text-default-500">{category.description}</p>
                                </div>
                                <Popover placement="bottom-end" showArrow offset={12}>
                                    <PopoverTrigger>
                                        <Button
                                            isIconOnly
                                            size="sm"
                                            variant="light"
                                            className={`h-8 w-8 border border-default-200 text-base font-semibold text-default-600 transition-colors hover:border-primary/60 hover:text-primary ${
                                                isSelected ? 'border-primary text-primary' : ''
                                            }`}
                                            aria-label={`Что входит в категорию ${category.label}`}
                                            onPress={(event) => event.stopPropagation()}
                                        >
                                            i
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-60 space-y-2 text-sm">
                                        <p className="text-xs uppercase tracking-[0.3em] text-default-500">
                                            Что можно сдавать
                                        </p>
                                        <Divider className="my-1" />
                                        <ul className="space-y-1 text-default-500">
                                            {category.info.map((item) => (
                                                <li key={item} className="flex items-center gap-2">
                                                    <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                                                    <span>{item}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    </PopoverContent>
                                </Popover>
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
