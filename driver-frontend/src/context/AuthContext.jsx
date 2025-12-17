import {createContext, useContext, useEffect, useMemo, useState} from "react";
import {apiFetch} from "../lib/apiClient.js";
import {API_BASE} from "../../cfg.js";

const AuthContext = createContext(null);

export function AuthProvider({children}) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const toDriverUser = (data) => {
        const role = data?.role || "";
        if (!String(role).includes("DRIVER")) {
            return null;
        }
        return {
            login: data.login,
            role: data.role,
        };
    };

    useEffect(() => {
        const fetchMe = async () => {
            try {
                const res = await apiFetch(`${API_BASE}/me`);
                if (res.ok) {
                    const data = await res.json();
                    setUser(toDriverUser(data));
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
        setUser(toDriverUser(userData));
    };

    const logout = async () => {
        try {
            await apiFetch(`${API_BASE}/logout`, {method: "POST"});
        } catch (e) {
            // ignore
        } finally {
            setUser(null);
        }
    };

    const value = useMemo(() => ({
        isAuthenticated: Boolean(user),
        isDriver: Boolean(user && String(user.role || "").includes("DRIVER")),
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
