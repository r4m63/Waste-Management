import {Moon, Sun} from "lucide-react";
import {Button} from "@/components/ui/button";
import {useTheme} from "./ThemeProvider.jsx";

export function ModeToggle() {
    const {theme, toggleTheme} = useTheme();

    return (
        <Button
            variant="outline"
            size="icon"
            className="rounded-full"
            type="button"
            onClick={toggleTheme}
        >
            <Sun className={`h-4 w-4 ${theme === "dark" ? "hidden" : "block"}`}/>
            <Moon className={`h-4 w-4 ${theme === "dark" ? "block" : "hidden"}`}/>
            <span className="sr-only">Toggle theme</span>
        </Button>
    );
}
