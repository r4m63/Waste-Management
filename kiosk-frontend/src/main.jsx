import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter} from 'react-router-dom'
import {NextUIProvider} from '@nextui-org/react'
import App from './App.jsx'
import './index.css'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <BrowserRouter>
            <NextUIProvider>
                <main className="min-h-screen bg-background text-foreground">
                    <App/>
                </main>
            </NextUIProvider>
        </BrowserRouter>
    </StrictMode>,
)
