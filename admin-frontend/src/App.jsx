// App.jsx

import {BrowserRouter, Route, Routes} from "react-router-dom"
import MainPage from "@/pages/MainPage.jsx";
import LoginPage from "@/pages/LoginPage.jsx";
import NotFoundPage from "@/pages/NotFoundPage.jsx";
import PointsPage from "@/pages/PointsPage.jsx";
import DashboardPage from "@/pages/DashboardPage.jsx";
import PosTerminalsPage from "@/pages/PosTerminalsPage.jsx";

export default function App() {
    return (
        <>
            <BrowserRouter>
                <Routes>
                    <Route path="/login" element={<LoginPage/>}/>

                    <Route element={<MainPage/>}>
                        <Route index element={<DashboardPage/>}/>
                        <Route path="points" element={<PointsPage/>}/>
                        <Route path="pos/terminals" element={<PosTerminalsPage/>}/>
                        <Route path="*" element={<NotFoundPage/>}/>
                    </Route>
                </Routes>
            </BrowserRouter>
        </>
    )
}
