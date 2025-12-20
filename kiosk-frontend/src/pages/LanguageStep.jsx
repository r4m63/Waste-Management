import {
    Button,
    Dropdown,
    DropdownItem,
    DropdownMenu,
    DropdownTrigger,
} from '@nextui-org/react'

const languages = [
    {code: 'ru', label: 'Русский', description: 'Интерфейс на русском языке', flag: '🇷🇺'},
    {code: 'en', label: 'English', description: 'Interface in English', flag: '🇬🇧'},
    {code: 'cn', label: '中文', description: '中文界面', flag: '🇨🇳'},
]

export default function LanguageStep({language, onLanguageChange}) {
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
                                onLanguageChange(value)
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
