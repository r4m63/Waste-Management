import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {apiFetch} from "@/lib/apiClient.js";
import {API_BASE} from "../../cfg.js";

const AuthContext = createContext(null);

export function AuthProvider({children}) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMe = async () => {
            try {
                const res = await apiFetch(`${API_BASE}/me`);
                if (res.ok) {
                    const data = await res.json();
                    setUser({login: data.login, role: data.role});
                } else {
                    setUser(null);
                }
            } catch (e) {
                setUser(null);
            } finally {
                setLoading(false);
            }
        };
        fetchMe();
    }, []);

    const login = (userData) => {
        setUser(userData);
    };

    const logout = async () => {
        try {
            await apiFetch(`${API_BASE}/logout`, {method: "POST"});
        } catch (e) {
            // ignore logout errors
        } finally {
            setUser(null);
        }
    };

    const value = useMemo(() => ({
        isAuthenticated: Boolean(user),
        user,
        loading,
        login,
        logout,
    }), [user, loading]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error("useAuth must be used within AuthProvider");
    }
    return ctx;
}
