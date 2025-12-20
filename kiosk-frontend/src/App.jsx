import {Navigate, Route, Routes, useLocation} from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login.jsx'
import {useAuth} from "./context/AuthContext.jsx";

export default function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={
                    <AnonOnly>
                        <Login/>
                    </AnonOnly>
                }
            />
            <Route
                path="/"
                element={
                    <Protected>
                        <Home/>
                    </Protected>
                }
            />
            <Route path="*" element={<Navigate to="/" replace/>}/>
        </Routes>
    )
}

function Protected({children}) {
    const {isAuthenticated, isKiosk, loading} = useAuth();
    const location = useLocation();

    if (loading) return null;
    if (!isAuthenticated || !isKiosk) {
        return <Navigate to="/login" replace state={{from: location}}/>;
    }
    return children;
}

function AnonOnly({children}) {
    const {isAuthenticated, isKiosk, loading} = useAuth();
    if (loading) return null;
    if (isAuthenticated && isKiosk) {
        return <Navigate to="/" replace/>;
    }
    return children;
}
