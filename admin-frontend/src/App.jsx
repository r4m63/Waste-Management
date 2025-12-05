// App.jsx

import {BrowserRouter, Route, Routes} from "react-router-dom"
import MainPage from "@/pages/MainPage.jsx";
import LoginPage from "@/pages/LoginPage.jsx";
import NotFoundPage from "@/pages/NotFoundPage.jsx";

export default function App() {
    return (
        <>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/" element={<MainPage/>}/>
                    <Route path="*" element={<NotFoundPage/>}/>
                </Routes>
            </BrowserRouter>
        </>
    )
}
