import {createContext, useContext, useEffect, useState} from "react";

const ThemeContext = createContext(null);

export function ThemeProvider({children}) {
    const [theme, setTheme] = useState(() => {
        if (typeof window === "undefined") return "light";

        const stored = localStorage.getItem("theme");
        if (stored === "light" || stored === "dark") return stored;

        // если нет сохранённого — берём системную
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        return prefersDark ? "dark" : "light";
    });

    useEffect(() => {
        const root = document.documentElement;

        root.classList.remove("light", "dark");
        root.classList.add(theme);

        localStorage.setItem("theme", theme);
    }, [theme]);

    const toggleTheme = () => setTheme((t) => (t === "dark" ? "light" : "dark"));

    return (
        <ThemeContext.Provider value={{theme, setTheme, toggleTheme}}>
            {children}
        </ThemeContext.Provider>
    );
}

export function useTheme() {
    const ctx = useContext(ThemeContext);
    if (!ctx) {
        throw new Error("useTheme must be used within ThemeProvider");
    }
    return ctx;
}
