// pages/LoginPage.jsx

import {ArrowRight, Lock, Mail} from "lucide-react"
import {Button} from "@/components/ui/button"
import {Input} from "@/components/ui/input"
import {Label} from "@/components/ui/label"
import {Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle,} from "@/components/ui/card"
import {Separator} from "@/components/ui/separator"
import {ModeToggle} from "@/components/provider/theme/ModeToggle.jsx"

export default function LoginPage() {
    return (
        <div className="min-h-screen bg-muted flex items-center justify-center px-4 relative">
            <div className="fixed right-4 top-4">
                <ModeToggle/>
            </div>

            <Card className="w-full max-w-md">
                <CardHeader>
                    <CardTitle className="text-2xl">Войти в аккаунт</CardTitle>
                    <CardDescription>Используйте свой логин и пароль.</CardDescription>
                </CardHeader>

                <CardContent className="space-y-4">
                    <form className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="login">Логин</Label>
                            <div className="relative">
                                <span
                                    className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-muted-foreground">
                                  <Mail className="h-4 w-4"/>
                                </span>
                                <Input
                                    id="login"
                                    className="pl-9"
                                    placeholder="Введите email"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="password">Пароль</Label>
                            <div className="relative">
                                <span
                                    className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-muted-foreground">
                                  <Lock className="h-4 w-4"/>
                                </span>
                                <Input
                                    id="password"
                                    type="password"
                                    className="pl-9"
                                    placeholder="Введите пароль"
                                />
                            </div>
                        </div>

                        <Button type="submit" className="w-full">
                            Войти
                            <ArrowRight className="ml-2 h-4 w-4"/>
                        </Button>
                    </form>

                    <Separator/>
                </CardContent>


                <CardFooter className="text-xs text-muted-foreground">
                    Нет доступа? Обратитесь к администратору.
                </CardFooter>
            </Card>
        </div>
    )
}
