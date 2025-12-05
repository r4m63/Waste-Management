// pages/NotFoundPage.jsx

import {Link} from "react-router-dom"
import {Button} from "@/components/ui/button"

export default function NotFoundPage() {
    return (
        <div className="flex flex-1 items-center justify-center">
            <div className="flex w-full flex-col items-center gap-4 rounded-lg border bg-muted/40 p-8 text-center md:max-w-xl">
                <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">404</p>
                    <h1 className="text-2xl font-semibold">Страница не найдена</h1>
                    <p className="text-muted-foreground">
                        Такой страницы нет. Проверьте адрес или выберите из меню.
                    </p>
                </div>
            </div>
        </div>
    )
}
