// App.jsx

import {BrowserRouter, Navigate, Route, Routes, useLocation} from "react-router-dom"
import MainPage from "@/pages/MainPage.jsx";
import LoginPage from "@/pages/LoginPage.jsx";
import NotFoundPage from "@/pages/NotFoundPage.jsx";
import PointsPage from "@/pages/PointsPage.jsx";
import DashboardPage from "@/pages/DashboardPage.jsx";
import PosTerminalsPage from "@/pages/PosTerminalsPage.jsx";
import PointsMapPage from "@/pages/PointsMapPage.jsx";
import VehiclesPage from "@/pages/VehiclesPage.jsx";
import DriversPage from "@/pages/DriversPage.jsx";
import KioskOrdersPage from "@/pages/KioskOrdersPage.jsx";
import {AuthProvider, useAuth} from "@/context/AuthContext.jsx";
import FractionsPage from "@/pages/FractionsPage.jsx";

export default function App() {
    return (
        <>
            <AuthProvider>
                <BrowserRouter>
                    <AppRoutes/>
                </BrowserRouter>
            </AuthProvider>
        </>
    )
}

function AppRoutes() {
    const {isAuthenticated, loading} = useAuth();

    if (loading) {
        return null;
    }

    return (
        <Routes>
            <Route
                path="/login"
                element={
                    isAuthenticated ? <Navigate to="/" replace/> : <LoginPage/>
                }
            />

            <Route element={<ProtectedRoute><MainPage/></ProtectedRoute>}>
                <Route index element={<DashboardPage/>}/>
                <Route path="points" element={<PointsPage/>}/>
                <Route path="points/map" element={<PointsMapPage/>}/>
                <Route path="pos/terminals" element={<PosTerminalsPage/>}/>
                <Route path="pos/operations" element={<KioskOrdersPage/>}/>
                <Route path="pos/fractions" element={<FractionsPage/>}/>
                <Route path="vehicles" element={<VehiclesPage/>}/>
                <Route path="vehicles/drivers" element={<DriversPage/>}/>
                <Route path="*" element={<NotFoundPage/>}/>
            </Route>
        </Routes>
    );
}

function ProtectedRoute({children}) {
    const {isAuthenticated, loading} = useAuth();
    const location = useLocation();

    if (loading) {
        return null;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace state={{from: location}}/>;
    }

    return children;
}
