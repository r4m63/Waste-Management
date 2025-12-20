import {useCallback, useEffect, useMemo, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {API_BASE} from '../../cfg.js'
import {apiFetch} from '../lib/apiClient.js'

function formatDateTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return String(iso)
  return d.toLocaleString()
}

function statusLabel(status) {
  switch (status) {
    case 'planned':
      return 'Запланирован'
    case 'in_progress':
      return 'В работе'
    case 'completed':
      return 'Завершён'
    case 'cancelled':
      return 'Отменён'
    default:
      return status || '—'
  }
}

function HomePage() {
  const navigate = useNavigate()
  const [routes, setRoutes] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [startingId, setStartingId] = useState(null)

  const fetchMyRoutes = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await apiFetch(`${API_BASE}/api/routes/my`)
      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }
      const data = await res.json()
      setRoutes(Array.isArray(data) ? data : [])
    } catch (e) {
      setRoutes([])
      setError(e instanceof Error ? e.message : 'Не удалось загрузить маршруты')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMyRoutes()
  }, [fetchMyRoutes])

  const handleStart = async (routeId) => {
    if (!routeId) return
    setStartingId(routeId)
    setError('')
    try {
      const res = await apiFetch(`${API_BASE}/api/routes/${routeId}/start`, {method: 'PUT'})
      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }
      await fetchMyRoutes()
      navigate(`/route/${routeId}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось начать маршрут')
    } finally {
      setStartingId(null)
    }
  }

  const hasRoutes = useMemo(() => routes.length > 0, [routes])

  return (
    <section className="panel">
      <header className="panel__header">
        <p className="eyebrow">Маршруты</p>
        <div style={{display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 12}}>
          <h1 className="panel__title">Назначенные маршруты</h1>
          <button type="button" className="button button--ghost button--sm" onClick={fetchMyRoutes} disabled={loading}>
            {loading ? 'Обновляем…' : 'Обновить'}
          </button>
        </div>
      </header>

      {error ? (
        <p className="panel__body" style={{color: '#fca5a5'}}>
          {error}
        </p>
      ) : null}

      {!hasRoutes && !loading ? (
        <p className="panel__body">Пока нет маршрутов, назначенных на ваш аккаунт.</p>
      ) : null}

      {hasRoutes ? (
        <div style={{overflowX: 'auto', marginTop: 12}}>
          <table style={{width: '100%', borderCollapse: 'collapse', minWidth: 720}}>
            <thead>
              <tr style={{textAlign: 'left', color: '#94a3b8', fontSize: 13}}>
                <th style={{padding: '10px 8px'}}>#</th>
                <th style={{padding: '10px 8px'}}>Статус</th>
                <th style={{padding: '10px 8px'}}>План</th>
                <th style={{padding: '10px 8px'}}>Старт</th>
                <th style={{padding: '10px 8px'}}>Финиш</th>
                <th style={{padding: '10px 8px'}}>Точки</th>
                <th style={{padding: '10px 8px'}} />
              </tr>
            </thead>
            <tbody>
              {routes.map((r) => {
                if (r.status !== 'planned' && r.status !== 'in_progress') return null
                const canStart = r.status === 'planned'
                const canOpen = r.status === 'in_progress'
                const stopsCount = Array.isArray(r.stops) ? r.stops.length : 0
                const stops = Array.isArray(r.stops) ? r.stops : []
                return (
                  <tr key={r.id} style={{borderTop: '1px solid rgba(255,255,255,0.06)'}}>
                    <td style={{padding: '10px 8px'}}>#{r.id}</td>
                    <td style={{padding: '10px 8px'}}>{statusLabel(r.status)}</td>
                    <td style={{padding: '10px 8px'}}>{r.plannedDate || '—'}</td>
                    <td style={{padding: '10px 8px'}}>{formatDateTime(r.plannedStartAt)}</td>
                    <td style={{padding: '10px 8px'}}>{formatDateTime(r.plannedEndAt)}</td>
                    <td style={{padding: '10px 8px'}}>
                      {stopsCount === 0 ? (
                        <span style={{color: '#94a3b8'}}>0</span>
                      ) : (
                        <details>
                          <summary style={{cursor: 'pointer', listStyle: 'none'}}>
                            <span style={{fontWeight: 700}}>{stopsCount}</span>{' '}
                            <span style={{color: '#94a3b8'}}>показать</span>
                          </summary>
                          <ol style={{margin: '10px 0 0', paddingLeft: 18, color: '#cbd5e1', lineHeight: 1.7}}>
                            {stops.map((s) => (
                              <li key={s.id || `${r.id}-${s.seqNo}`}>
                                {s.address || '—'}{' '}
                                {s.expectedCapacity != null ? (
                                  <span style={{color: '#94a3b8'}}>(ожид. {s.expectedCapacity})</span>
                                ) : null}
                              </li>
                            ))}
                          </ol>
                        </details>
                      )}
                    </td>
                    <td style={{padding: '10px 8px', textAlign: 'right'}}>
                      {canStart ? (
                        <button
                          type="button"
                          className="button button--sm"
                          onClick={() => handleStart(r.id)}
                          disabled={startingId === r.id}
                        >
                          {startingId === r.id ? 'Начинаем…' : 'Начать'}
                        </button>
                      ) : canOpen ? (
                        <button type="button" className="button button--ghost button--sm" onClick={() => navigate(`/route/${r.id}`)}>
                          Открыть
                        </button>
                      ) : (
                        <span style={{color: '#94a3b8', fontSize: 13}}>—</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  )
}

export default HomePage
