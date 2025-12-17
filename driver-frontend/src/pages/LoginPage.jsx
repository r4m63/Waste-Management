import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { apiFetch } from '../lib/apiClient.js'
import { API_BASE } from '../../cfg.js'

function LoginPage() {
  const { login: setUser } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [userLogin, setUserLogin] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!userLogin.trim() || !password.trim()) {
      setError('Введите логин и пароль')
      return
    }
    setError('')
    setLoading(true)

    try {
      const res = await apiFetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login: userLogin, password }),
      })

      if (!res.ok) {
        const data = await res.json().catch(() => null)
        setError(data?.error || 'Неверный логин или пароль')
        return
      }

      const data = await res.json()
      const roleStr = data.role || ''
      if (!roleStr.includes('DRIVER')) {
        setError('Доступ разрешён только пользователям с ролью DRIVER')
        return
      }

      setUser({ login: data.login, role: data.role })
      const redirectTo = location.state?.from?.pathname || '/routes'
      navigate(redirectTo, { replace: true })
    } catch (e) {
      setError('Ошибка подключения')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth">
      <div className="auth__card">
        <header className="auth__header">
          <p className="eyebrow">Driver Portal</p>
          <h1 className="auth__title">Вход</h1>
          <p className="auth__hint">Используйте логин и пароль, выданные оператором.</p>
        </header>

        <form className="auth__form" onSubmit={handleSubmit}>
          <label className="field">
            <span className="field__label">Логин</span>
            <input
              type="text"
              className="field__input"
              placeholder="driver01"
              value={userLogin}
              onChange={(e) => setUserLogin(e.target.value)}
              required
              disabled={loading}
            />
          </label>

          <label className="field">
            <span className="field__label">Пароль</span>
            <input
              type="password"
              className="field__input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </label>

          {error ? <p className="field__error">{error}</p> : null}

          <button type="submit" className="button button--full" disabled={loading}>
            {loading ? 'Входим…' : 'Войти'}
          </button>
        </form>
      </div>
    </div>
  )
}

export default LoginPage
