import {useState} from "react";
import {Button, Card, CardBody, CardFooter, CardHeader, Input, Spacer} from "@nextui-org/react";
import {useLocation, useNavigate} from "react-router-dom";
import {apiFetch} from "../lib/apiClient.js";
import {API_BASE} from "../../cfg.js";
import {useAuth} from "../context/AuthContext.jsx";

export default function Login() {
    const [login, setLogin] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const {login: setUser} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const redirectTo = location.state?.from?.pathname || "/";

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const res = await apiFetch(`${API_BASE}/login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({login, password}),
            });

            if (!res.ok) {
                const data = await res.json().catch(() => null);
                setError(data?.error || "Неверный логин или пароль");
                return;
            }

            const data = await res.json();
            const roleStr = data.role || "";
            if (!roleStr.includes("KIOSK")) {
                setError("Доступ разрешён только пользователям с ролью KIOSK");
                return;
            }

            setUser({login: data.login, role: data.role});
            navigate(redirectTo, {replace: true});
        } catch (err) {
            setError("Ошибка подключения");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-background flex items-center justify-center px-4">
            <Card className="w-full max-w-md">
                <CardHeader className="flex flex-col gap-1">
                    <h1 className="text-2xl font-semibold">Вход для киосков</h1>
                    <p className="text-sm text-default-500">Введите логин и пароль учётной записи KIOSK.</p>
                </CardHeader>
                <CardBody as="form" onSubmit={handleSubmit} className="gap-4">
                    <Input
                        label="Логин"
                        variant="bordered"
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                        required
                        autoFocus
                    />
                    <Input
                        label="Пароль"
                        type="password"
                        variant="bordered"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    {error && <p className="text-danger text-sm">{error}</p>}
                    <Spacer y={1}/>
                    <Button color="primary" type="submit" isLoading={loading}>
                        Войти
                    </Button>
                </CardBody>
                <CardFooter className="text-xs text-default-500">
                    Если у вас нет доступа, обратитесь к администратору.
                </CardFooter>
            </Card>
        </div>
    );
}
