import {Navigate, NavLink, Outlet, Route, Routes, useLocation} from 'react-router-dom'
import {useAuth} from './context/AuthContext.jsx'
import HomePage from './pages/HomePage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RoutePage from './pages/RoutePage.jsx'
import './App.css'

const navLinkClass = ({isActive}) => (isActive ? 'nav__link nav__link--active' : 'nav__link')

function Layout() {
    const {user, logout} = useAuth()

    return (
        <div className="app">
            <header className="app__header">
                <div className="brand">
                    <span className="brand__dot" aria-hidden/>
                    Driver Portal
                </div>
                <nav className="nav">
                    <NavLink to="/" end className={navLinkClass}>
                        Маршруты
                    </NavLink>
                </nav>
                <div className="user">
                    <span className="user__name">{user?.login || 'Водитель'}</span>
                    <button type="button" className="button button--ghost button--sm" onClick={logout}>
                        Выйти
                    </button>
                </div>
            </header>
            <main className="app__content">
                <Outlet/>
            </main>
        </div>
    )
}

export default function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={
                    <AnonOnly>
                        <LoginPage/>
                    </AnonOnly>
                }
            />
            <Route
                path="/"
                element={
                    <Protected>
                        <Layout/>
                    </Protected>
                }
            >
                <Route index element={<HomePage/>}/>
                <Route path="route/:id" element={<RoutePage/>}/>
            </Route>
            <Route path="*" element={<Navigate to="/" replace/>}/>
        </Routes>
    )
}

function Protected({children}) {
    const {isAuthenticated, isDriver, loading} = useAuth()
    const location = useLocation()

    if (loading) return null
    if (!isAuthenticated || !isDriver) {
        return <Navigate to="/login" replace state={{from: location}}/>
    }
    return children
}

function AnonOnly({children}) {
    const {isAuthenticated, isDriver, loading} = useAuth()
    if (loading) return null
    if (isAuthenticated && isDriver) {
        return <Navigate to="/" replace/>
    }
    return children
}
