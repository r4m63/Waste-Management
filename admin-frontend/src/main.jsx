// main.jsx

import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {ThemeProvider} from "@/components/provider/theme/ThemeProvider.jsx";
import {StrictMode} from "react";

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <ThemeProvider>
            <App/>
        </ThemeProvider>
    </StrictMode>
)
