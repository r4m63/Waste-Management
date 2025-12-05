import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Chip,
  Divider,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  DropdownTrigger,
  Input,
  Progress,
  Spinner,
} from '@nextui-org/react'

const DEFAULT_LANGUAGE = 'ru'

const languages = [
  { code: 'ru', label: 'Русский', description: 'Интерфейс на русском языке', flag: '🇷🇺' },
  { code: 'en', label: 'English', description: 'Interface in English', flag: '🇬🇧' },
  { code: 'cn', label: '中文', description: '中文界面', flag: '🇨🇳' },
]

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

  const renderLanguageStep = () => {
    const selectedLanguage = languages.find((lang) => lang.code === language)
    return (
      <div className="relative flex min-h-[420px] flex-col items-center justify-center gap-6 pb-24 text-center">
        <div className="space-y-3">
          <p className="text-sm font-medium uppercase tracking-[0.4em] text-primary/80">
            Waste Management
          </p>
          <h2 className="text-4xl font-semibold text-foreground md:text-5xl">
            Киоск терминал приёма отходов
          </h2>
          <p className="text-default-500">
            Добро пожаловать! Выберите язык интерфейса, чтобы начать работу с терминалом.
          </p>
        </div>

        <div className="absolute bottom-0 right-0">
          <Dropdown placement="top-end">
            <DropdownTrigger>
              <Button
                variant="bordered"
                className="h-16 w-16 rounded-full border-primary/30 text-4xl"
                aria-label="Выбор языка интерфейса"
              >
                {selectedLanguage?.flag ?? '🌐'}
              </Button>
            </DropdownTrigger>
            <DropdownMenu
              aria-label="Выбор языка"
              selectionMode="single"
              selectedKeys={language ? new Set([language]) : new Set()}
              onSelectionChange={(keys) => {
                const value = Array.from(keys).pop()
                if (typeof value === 'string') {
                  setLanguage(value)
                }
              }}
              itemClasses={{
                base: 'data-[hover=true]:bg-primary/10 text-foreground',
                title: 'text-foreground',
                description: 'text-default-500',
              }}
            >
              {languages.map((lang) => (
                <DropdownItem
                  key={lang.code}
                  startContent={<span className="text-2xl">{lang.flag}</span>}
                  description={lang.description}
                >
                  {lang.label}
                </DropdownItem>
              ))}
            </DropdownMenu>
          </Dropdown>
        </div>
      </div>
    )
  }

  const renderPhoneStep = () => (
    <div className="flex flex-col gap-4">
      <Input
        type="tel"
        label="Номер телефона"
        placeholder="+7 (___) ___-__-__"
        value={phone}
        onValueChange={setPhone}
        variant="bordered"
        classNames={{
          input: 'text-lg',
          label: 'text-base',
        }}
        description="Укажите контакт для уведомлений или пропустите шаг"
      />
      <div className="flex flex-wrap gap-3">
        <Button variant="flat" color="secondary" onPress={() => setPhone('')}>
          Стереть номер
        </Button>
        <Button variant="bordered" onPress={goNext}>
          Пропустить
        </Button>
      </div>
    </div>
  )

  const renderContainerStep = () => (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {containerSizes.map((size) => {
        const isSelected = containerSize === size.id
        return (
          <Card
            key={size.id}
            isPressable
            onPress={() => setContainerSize(size.id)}
            className={`relative border-2 transition-all ${
              isSelected ? 'border-primary shadow-large' : 'border-default-100 bg-content2/40'
            }`}
          >
            <CardBody className="flex min-h-[170px] flex-col gap-4">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="text-xs uppercase tracking-[0.25em] text-default-500">Размер</p>
                  <h3 className="text-3xl font-semibold">{size.label}</h3>
                  <p className="text-small text-default-500">{size.description}</p>
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
                      aria-label={`Габариты контейнера ${size.label}`}
                    >
                      i
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-56 space-y-1 text-sm">
                    <p className="text-xs uppercase tracking-[0.3em] text-default-500">Габариты</p>
                    <Divider className="my-2" />
                    <div className="space-y-1 text-default-500">
                      <p className="flex items-center gap-2">
                        <span className="w-20 text-left">Ширина:</span>
                        <span className="text-foreground font-semibold">{size.width} см</span>
                      </p>
                      <p className="flex items-center gap-2">
                        <span className="w-20 text-left">Длина:</span>
                        <span className="text-foreground font-semibold">{size.length} см</span>
                      </p>
                      <p className="flex items-center gap-2">
                        <span className="w-20 text-left">Высота:</span>
                        <span className="text-foreground font-semibold">{size.height} см</span>
                      </p>
                    </div>
                  </PopoverContent>
                </Popover>
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

  const renderWasteStep = () => (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {wasteCategories.map((category) => {
        const isSelected = wasteType === category.id
        return (
          <Card
            key={category.id}
            isPressable
            onPress={() => setWasteType(category.id)}
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
                    <p className="text-xs uppercase tracking-[0.3em] text-default-500">Что можно сдавать</p>
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
                  <Chip color="primary" variant="flat">
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

  const renderWeighingStep = () => (
    <div className="space-y-6">
      <Card className="bg-content2/50">
        <CardBody className="space-y-3">
          <p className="text-sm uppercase tracking-[0.2em] text-default-500">Инструкция</p>
          <h3 className="text-2xl font-semibold">Положите отходы в контейнер-приёмник</h3>
          <p className="text-default-500">
            Система автоматически начнёт взвешивание при закрытии крышки. После загрузки нажмите кнопку ниже, процесс занимает около 1 секунды.
          </p>
        </CardBody>
      </Card>

      <div className="flex flex-col items-center gap-4 text-center">
        <Button
          color="primary"
          size="lg"
          className="w-full max-w-md justify-center rounded-2xl text-lg shadow-lg shadow-primary/40"
          isDisabled={isWeighing}
          onPress={startWeighing}
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
            onPress={handleConfirm}
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

  const renderStep = () => {
    switch (step) {
      case 0:
        return renderLanguageStep()
      case 1:
        return renderPhoneStep()
      case 2:
        return renderContainerStep()
      case 3:
        return renderWasteStep()
      case 4:
        return renderWeighingStep()
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
        <Progress value={progressValue} color="primary" className="bg-content2/40" />
      </div>

      <Card className="flex-1 bg-content1/80 shadow-large backdrop-blur">
        <CardHeader className="flex flex-col gap-1">
          <h2 className="text-2xl font-semibold">{currentStepTitle}</h2>
        </CardHeader>
        <Divider />
        <CardBody className="py-6">{renderStep()}</CardBody>
        <Divider />
        <CardFooter className="flex flex-col gap-3 sm:flex-row sm:justify-between">
          <Button variant="bordered" color="secondary" className="w-full sm:w-auto" onPress={goPrev} isDisabled={step === 0}>
            Назад
          </Button>
          {step < totalSteps - 1 && (
            <Button color="primary" className="w-full sm:w-auto" onPress={goNext} isDisabled={!canProceed}>
              Далее
            </Button>
          )}
        </CardFooter>
      </Card>
    </section>
    )
}
