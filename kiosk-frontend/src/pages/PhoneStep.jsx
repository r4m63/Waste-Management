import {Button, Card, CardBody} from '@nextui-org/react'
import Cleave from 'cleave.js/react'

const MAX_RU_PHONE_DIGITS = 10 // 10 цифр после +7
const DIGIT_KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '0']

// Маска: +7 (XXX) XXX-XX-XX
// Без цифр: +7 (___) ___-__-__
const formatPhone = (digits) => {
    const cleaned = (digits || '').replace(/\D/g, '').slice(0, MAX_RU_PHONE_DIGITS)
    const chars = cleaned.split('')
    const get = (i) => chars[i] ?? '_'

    return `+7 (${get(0)}${get(1)}${get(2)}) ${get(3)}${get(4)}${get(5)}-${get(6)}${get(7)}-${get(8)}${get(9)}`
}

export default function PhoneStep({phone, onPhoneChange}) {
    // phone — только цифры после +7, например "9112345678"
    const digits = (phone ?? '').replace(/\D/g, '').slice(0, MAX_RU_PHONE_DIGITS)
    const formattedValue = formatPhone(digits)
    const digitsCount = digits.length

    // Ввод с физической клавиатуры
    const handleInputChange = (event) => {
        const raw = (event.target.value ?? '').toString()
        // вытаскиваем все цифры из строки "+7 (9_ _) ___-__-__"
        let onlyDigits = raw.replace(/\D/g, '')

        // первая цифра всегда "7" из "+7", убираем её
        if (onlyDigits.startsWith('7')) {
            onlyDigits = onlyDigits.slice(1)
        }

        const normalized = onlyDigits.slice(0, MAX_RU_PHONE_DIGITS)
        onPhoneChange(normalized)
    }

    // Экранная клавиатура: цифры
    const handleDigitClick = (digit) => {
        if (digitsCount >= MAX_RU_PHONE_DIGITS) return
        const next = (digits + digit).slice(0, MAX_RU_PHONE_DIGITS)
        onPhoneChange(next)
    }

    const handleBackspace = () => {
        if (!digitsCount) return
        const next = digits.slice(0, -1)
        onPhoneChange(next)
    }

    const handleClear = () => {
        onPhoneChange('')
    }

    return (
        <div className="flex flex-col gap-6">
            {/* Поле ввода */}
            <div className="flex flex-col gap-2">
                <label
                    className="text-base font-medium text-foreground"
                    htmlFor="phone-input"
                >
                    Номер телефона
                </label>

                <Cleave
                    id="phone-input"
                    type="tel"
                    value={formattedValue}
                    options={{
                        numericOnly: true, // только цифры с физической клавиатуры
                    }}
                    onChange={handleInputChange}
                    placeholder="+7 (___) ___-__-__"
                    className="h-14 w-full rounded-2xl border border-default-200 bg-content2 px-4 text-lg outline-none transition-colors focus-visible:border-primary"
                />
            </div>

            {/* Экранная цифровая клавиатура по центру */}
            <Card className="max-w-sm bg-content2/60 mx-auto">
                <CardBody className="space-y-3">

                    {/* Сетка цифр + backspace */}
                    <div className="grid grid-cols-3 gap-3">
                        {/* 1–9 */}
                        {DIGIT_KEYS.slice(0, 9).map((key) => (
                            <Button
                                key={key}
                                variant="shadow"
                                color="primary"
                                className="h-12 text-lg"
                                onPress={() => handleDigitClick(key)}
                                isDisabled={digitsCount >= MAX_RU_PHONE_DIGITS}
                            >
                                {key}
                            </Button>
                        ))}

                        {/* ⌫, 0 и пустая ячейка для ровной сетки */}
                        <Button
                            variant="flat"
                            color="primary"
                            className="h-12 text-lg"
                            onPress={handleBackspace}
                        >
                            ⌫
                        </Button>

                        <Button
                            variant="shadow"
                            color="primary"
                            className="h-12 text-lg"
                            onPress={() => handleDigitClick('0')}
                            isDisabled={digitsCount >= MAX_RU_PHONE_DIGITS}
                        >
                            0
                        </Button>

                        <div/>
                    </div>

                    {/* Кнопка "Очистить" по центру */}
                    <div className="mt-1 flex justify-center">
                        <Button
                            variant="flat"
                            color="danger"
                            className="h-12 px-10 text-lg"
                            onPress={handleClear}
                        >
                            Очистить
                        </Button>
                    </div>
                </CardBody>
            </Card>
        </div>
    )
}
