import {
    Button,
    Card,
    CardBody,
    Chip,
    Divider,
    Spinner,
} from '@nextui-org/react'

export default function WeighingStep({
                                         isWeighing,
                                         weight,
                                         confirmed,
                                         onStartWeighing,
                                         onConfirm,
                                     }) {
    return (
        <div className="space-y-6">
            <Card className="bg-content2/50">
                <CardBody className="space-y-3">
                    <p className="text-sm uppercase tracking-[0.2em] text-default-500">Инструкция</p>
                    <h3 className="text-2xl font-semibold">Положите отходы в контейнер-приёмник</h3>
                    <p className="text-default-500">
                        Система автоматически начнёт взвешивание при закрытии крышки.<br/>После загрузки нажмите кнопку
                        ниже, процесс занимает около 1 секунды.
                    </p>
                </CardBody>
            </Card>

            <div className="flex flex-col items-center gap-4 text-center">
                <Button
                    color="primary"
                    size="lg"
                    className="w-full max-w-md justify-center rounded-2xl text-lg shadow-lg shadow-primary/40"
                    isDisabled={isWeighing}
                    onPress={onStartWeighing}
                >
                    {isWeighing ? (
                        <div className="flex items-center gap-2">
                            <Spinner size="sm" color="white" />
                            Взвешивание...
                        </div>
                    ) : weight ? (
                        'Перевесить'
                    ) : (
                        'Начать взвешивание'
                    )}
                </Button>
                {weight && (
                    <Chip color="primary" size="lg" variant="shadow" className="text-xl">
                        {weight} кг
                    </Chip>
                )}
            </div>

            {weight && (
                <div className="flex w-full flex-col items-center">
                    <Button
                        color="success"
                        size="lg"
                        className="w-full max-w-md justify-center rounded-2xl text-lg shadow-lg shadow-success/30"
                        onPress={onConfirm}
                        isDisabled={isWeighing}
                    >
                        Подтвердить приём
                    </Button>
                </div>
            )}

            {confirmed && (
                <Card className="border border-success bg-success/10">
                    <CardBody className="flex items-center gap-3 text-success">
                        <Chip color="success" variant="flat">
                            Готово
                        </Chip>
                        <p className="text-lg font-semibold">Приём отходов подтверждён, спасибо!</p>
                    </CardBody>
                </Card>
            )}
        </div>
    )
}
