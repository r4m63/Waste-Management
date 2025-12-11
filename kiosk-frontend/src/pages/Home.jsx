import {useEffect, useMemo, useRef, useState} from 'react'
import {Button, Card, CardBody, CardFooter, CardHeader, Divider, Progress,} from '@nextui-org/react'

import LanguageStep from './LanguageStep'
import PhoneStep from './PhoneStep'
import ContainerStep from './ContainerStep'
import WasteStep from './WasteStep'
import WeighingStep from './WeighingStep'

const DEFAULT_LANGUAGE = 'ru'
const totalSteps = 5

export default function Home() {
    const [step, setStep] = useState(0)
    const [language, setLanguage] = useState(DEFAULT_LANGUAGE)
    const [phone, setPhone] = useState('')
    const [containerSize, setContainerSize] = useState(null)
    const [wasteType, setWasteType] = useState(null)
    const [isWeighing, setIsWeighing] = useState(false)
    const [weight, setWeight] = useState(null)
    const [confirmed, setConfirmed] = useState(false)
    const weighingTimer = useRef()
    const resetTimer = useRef()

    useEffect(() => {
        return () => {
            if (weighingTimer.current) {
                clearTimeout(weighingTimer.current)
            }
            if (resetTimer.current) {
                clearTimeout(resetTimer.current)
            }
        }
    }, [])

    const currentStepTitle = [
        'Приветствие и выбор языка',
        'Введите номер телефона или пропустите шаг',
        'Выберите размер контейнера',
        'Выберите тип отходов',
        'Загрузите отходы в контейнер-приёмник и взвесьте',
    ][step]

    const currentStepLabel = ['Язык', 'Телефон', 'Контейнер', 'Отходы', 'Взвешивание'][step]

    const progressValue = useMemo(() => ((step + 1) / totalSteps) * 100, [step])

    const canProceed = useMemo(() => {
        switch (step) {
            case 0:
                return Boolean(language)
            case 2:
                return Boolean(containerSize)
            case 3:
                return Boolean(wasteType)
            case 4:
                return Boolean(weight)
            default:
                return true
        }
    }, [step, language, containerSize, wasteType, weight])

    const goNext = () => {
        if (step < totalSteps - 1) {
            setStep((prev) => prev + 1)
        }
    }

    const goPrev = () => {
        if (step > 0) {
            setStep((prev) => prev - 1)
        }
    }

    const startWeighing = () => {
        if (isWeighing) return
        setConfirmed(false)
        setWeight(null)
        setIsWeighing(true)
        weighingTimer.current = setTimeout(() => {
            const measuredWeight = (Math.random() * 80 + 2).toFixed(1)
            setWeight(measuredWeight)
            setIsWeighing(false)
        }, 1000)
    }

    const handleConfirm = () => {
        if (!weight || isWeighing) return
        setConfirmed(true)
        if (resetTimer.current) {
            clearTimeout(resetTimer.current)
        }
        resetTimer.current = setTimeout(() => {
            setStep(0)
            setLanguage(DEFAULT_LANGUAGE)
            setPhone('')
            setContainerSize(null)
            setWasteType(null)
            setWeight(null)
            setIsWeighing(false)
            setConfirmed(false)
        }, 1500)
    }

    const renderStep = () => {
        switch (step) {
            case 0:
                return (
                    <LanguageStep
                        language={language}
                        onLanguageChange={setLanguage}
                    />
                )
            case 1:
                return (
                    <PhoneStep phone={phone} onPhoneChange={setPhone}/>
                )
            case 2:
                return (
                    <ContainerStep
                        containerSize={containerSize}
                        onSelectSize={setContainerSize}
                    />
                )
            case 3:
                return (
                    <WasteStep
                        wasteType={wasteType}
                        onSelectWasteType={setWasteType}
                    />
                )
            case 4:
                return (
                    <WeighingStep
                        isWeighing={isWeighing}
                        weight={weight}
                        confirmed={confirmed}
                        onStartWeighing={startWeighing}
                        onConfirm={handleConfirm}
                    />
                )
            default:
                return null
        }
    }

    return (
        <section className="mx-auto flex min-h-screen w-full max-w-6xl flex-col gap-6 px-4 py-8 md:px-10">
            <div className="space-y-3">
                <div className="flex items-center justify-between text-sm text-default-500">
                    <span>Шаг {step + 1} из {totalSteps}</span>
                    <span>{currentStepLabel}</span>
                </div>
                <Progress
                    value={progressValue}
                    color="primary"
                    className="bg-content2/40"
                    aria-label={`Прогресс: шаг ${step + 1} из ${totalSteps}`}
                />
            </div>

            <Card className="flex-1 bg-content1/80 shadow-large backdrop-blur">
                <CardHeader className="flex flex-col gap-1">
                    <h2 className="text-2xl font-semibold">{currentStepTitle}</h2>
                </CardHeader>
                <Divider/>
                <CardBody className="py-6">
                    {renderStep()}
                </CardBody>
                <Divider/>
                <CardFooter className="flex flex-col gap-4 sm:flex-row sm:justify-between">
                    <Button
                        variant="bordered"
                        color="secondary"
                        size="lg"
                        className="w-full sm:w-auto h-14 rounded-2xl text-lg px-8"
                        onPress={goPrev}
                        isDisabled={step === 0}
                    >
                        Назад
                    </Button>

                    {step < totalSteps - 1 && (
                        <Button
                            color="primary"
                            size="lg"
                            className="w-full sm:w-auto h-14 rounded-2xl text-lg px-10"
                            onPress={goNext}
                            isDisabled={!canProceed}
                        >
                            Далее
                        </Button>
                    )}
                </CardFooter>

            </Card>
        </section>
    )
}
