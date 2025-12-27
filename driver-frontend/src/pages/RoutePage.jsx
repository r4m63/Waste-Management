import {useCallback, useEffect, useMemo, useState} from 'react'
import {Link, useNavigate, useParams} from 'react-router-dom'
import {API_BASE} from '../../cfg.js'
import {apiFetch} from '../lib/apiClient.js'

const STOP_STATUSES = [
  'planned',
  'enroute',
  'arrived',
  'loading',
  'unloading',
  'done',
  'skipped',
  'unavailable',
]

const INCIDENT_TYPES = [
  {value: 'access_denied', label: 'Нет доступа', icon: '🚫'},
  {value: 'traffic', label: 'Пробки', icon: '🚗'},
  {value: 'vehicle_issue', label: 'Проблема с ТС', icon: '🔧'},
  {value: 'overload', label: 'Перегруз', icon: '📦'},
  {value: 'other', label: 'Другое', icon: '❓'},
]

function stopStatusLabel(status) {
  switch (status) {
    case 'planned':
      return 'Запланировано'
    case 'enroute':
      return 'В пути'
    case 'arrived':
      return 'Прибыл'
    case 'loading':
      return 'Погрузка'
    case 'unloading':
      return 'Разгрузка'
    case 'done':
      return 'Готово'
    case 'skipped':
      return 'Пропущено'
    case 'unavailable':
      return 'Недоступно'
    default:
      return status || '—'
  }
}

function isTerminalStopStatus(status) {
  return status === 'done' || status === 'skipped' || status === 'unavailable'
}

function RoutePage() {
  const navigate = useNavigate()
  const {id} = useParams()
  const routeId = Number(id)
  const [route, setRoute] = useState(null)
  const [draftByStopId, setDraftByStopId] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [savingStopId, setSavingStopId] = useState(null)
  const [finishing, setFinishing] = useState(false)

  // Incident modal state
  const [incidentModal, setIncidentModal] = useState({open: false, stopId: null, stopAddress: ''})
  const [incidentType, setIncidentType] = useState('')
  const [incidentDescription, setIncidentDescription] = useState('')
  const [incidentSubmitting, setIncidentSubmitting] = useState(false)
  const [incidentError, setIncidentError] = useState('')

  const fetchRoute = useCallback(async () => {
    if (!routeId) return
    setLoading(true)
    setError('')
    try {
      const res = await apiFetch(`${API_BASE}/api/routes/${routeId}/my`)
      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }
      const data = await res.json()
      setRoute(data || null)
    } catch (e) {
      setRoute(null)
      setError(e instanceof Error ? e.message : 'Не удалось загрузить маршрут')
    } finally {
      setLoading(false)
    }
  }, [routeId])

  useEffect(() => {
    fetchRoute()
  }, [fetchRoute])

  useEffect(() => {
    const stops = Array.isArray(route?.stops) ? route.stops : []
    const nextDraft = {}
    for (const s of stops) {
      nextDraft[s.id] = {
        status: s.status || 'planned',
        actualCapacity: s.actualCapacity ?? '',
        note: s.note ?? '',
      }
    }
    setDraftByStopId(nextDraft)
  }, [route])

  const stops = useMemo(() => (Array.isArray(route?.stops) ? route.stops : []), [route])

  const currentStopId = useMemo(() => {
    const current = stops.find((s) => !isTerminalStopStatus(s.status))
    return current?.id ?? null
  }, [stops])

  const routeIsReadOnly = route?.status !== 'in_progress'

  const updateDraft = (stopId, patch) => {
    setDraftByStopId((prev) => ({
      ...prev,
      [stopId]: {
        ...(prev[stopId] || {}),
        ...patch,
      },
    }))
  }

  const handleSave = async (stopId) => {
    if (!routeId || !stopId) return
    setSavingStopId(stopId)
    setError('')
    try {
      const draft = draftByStopId[stopId]
      const payload = {
        status: draft?.status || 'planned',
        actualCapacity: draft?.actualCapacity === '' ? null : Number(draft?.actualCapacity),
        note: draft?.note || null,
      }

      const res = await apiFetch(`${API_BASE}/api/routes/${routeId}/stops/${stopId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }
      await fetchRoute()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось обновить остановку')
    } finally {
      setSavingStopId(null)
    }
  }

  const handleFinish = async () => {
    if (!routeId) return
    if (!window.confirm('Завершить маршрут? Все незавершённые остановки будут помечены как пропущенные.')) return
    setFinishing(true)
    setError('')
    try {
      const res = await apiFetch(`${API_BASE}/api/routes/${routeId}/finish`, {method: 'PUT'})
      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }
      navigate('/', {replace: true})
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Не удалось завершить маршрут')
    } finally {
      setFinishing(false)
    }
  }

  // Incident handlers
  const openIncidentModal = (stop) => {
    setIncidentModal({open: true, stopId: stop.id, stopAddress: stop.address || `Точка #${stop.seqNo}`})
    setIncidentType('')
    setIncidentDescription('')
    setIncidentError('')
  }

  const closeIncidentModal = () => {
    setIncidentModal({open: false, stopId: null, stopAddress: ''})
    setIncidentType('')
    setIncidentDescription('')
    setIncidentError('')
  }

  const handleIncidentSubmit = async () => {
    if (!incidentType) {
      setIncidentError('Выберите тип инцидента')
      return
    }
    if (!incidentModal.stopId) return

    setIncidentSubmitting(true)
    setIncidentError('')
    try {
      const payload = {
        stopId: incidentModal.stopId,
        type: incidentType,
        description: incidentDescription || null,
      }

      const res = await apiFetch(`${API_BASE}/api/incidents`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(payload),
      })

      if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(text || `Ошибка: ${res.status} ${res.statusText}`)
      }

      // Update stop status to unavailable
      await apiFetch(`${API_BASE}/api/routes/${routeId}/stops/${incidentModal.stopId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify({
          status: 'unavailable',
          actualCapacity: null,
          note: `Инцидент: ${INCIDENT_TYPES.find(t => t.value === incidentType)?.label || incidentType}`,
        }),
      })

      closeIncidentModal()
      await fetchRoute()
    } catch (e) {
      setIncidentError(e instanceof Error ? e.message : 'Не удалось создать инцидент')
    } finally {
      setIncidentSubmitting(false)
    }
  }

  return (
    <>
      <section className="panel">
        <header className="panel__header">
          <p className="eyebrow">Выполнение маршрута</p>
          <div style={{display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 12}}>
            <h1 className="panel__title">Маршрут #{routeId}</h1>
            <div style={{display: 'flex', gap: 10}}>
              <button type="button" className="button button--ghost button--sm" onClick={fetchRoute} disabled={loading}>
                {loading ? 'Обновляем…' : 'Обновить'}
              </button>
              <Link to="/" className="button button--ghost button--sm">
                Назад
              </Link>
            </div>
          </div>
        </header>

        {error ? (
          <p className="panel__body" style={{color: '#fca5a5'}}>
            {error}
          </p>
        ) : null}

        {route ? (
          <p className="panel__body" style={{marginTop: 8}}>
            Статус маршрута: <strong>{route.status}</strong>
            {routeIsReadOnly ? (
              <span style={{color: '#94a3b8'}}> (редактирование недоступно)</span>
            ) : null}
          </p>
        ) : null}

        {stops.length ? (
          <div style={{overflowX: 'auto', marginTop: 14}}>
            <table style={{width: '100%', borderCollapse: 'collapse', minWidth: 1000}}>
              <thead>
                <tr style={{textAlign: 'left', color: '#94a3b8', fontSize: 13}}>
                  <th style={{padding: '10px 8px'}}>№</th>
                  <th style={{padding: '10px 8px'}}>Адрес</th>
                  <th style={{padding: '10px 8px'}}>Статус</th>
                  <th style={{padding: '10px 8px'}}>Ожид.</th>
                  <th style={{padding: '10px 8px'}}>Факт</th>
                  <th style={{padding: '10px 8px'}}>Комментарий</th>
                  <th style={{padding: '10px 8px'}} />
                  <th style={{padding: '10px 8px'}} />
                </tr>
              </thead>
              <tbody>
                {stops.map((s) => {
                  const isCurrent = currentStopId === s.id
                  const draft = draftByStopId[s.id] || {status: s.status, actualCapacity: s.actualCapacity ?? '', note: s.note ?? ''}
                  const isTerminal = isTerminalStopStatus(s.status)
                  return (
                    <tr
                      key={s.id}
                      style={{
                        borderTop: '1px solid rgba(255,255,255,0.06)',
                        background: isCurrent ? 'rgba(103,232,249,0.06)' : 'transparent',
                      }}
                    >
                      <td style={{padding: '10px 8px'}}>{s.seqNo}</td>
                      <td style={{padding: '10px 8px', color: '#cbd5e1'}}>{s.address || '—'}</td>
                      <td style={{padding: '10px 8px'}}>
                        <select
                          value={draft.status || 'planned'}
                          onChange={(e) => updateDraft(s.id, {status: e.target.value})}
                          disabled={routeIsReadOnly}
                          style={{
                            padding: '8px 10px',
                            borderRadius: 10,
                            border: '1px solid rgba(255,255,255,0.12)',
                            background: 'rgba(255,255,255,0.06)',
                            color: '#f8fafc',
                          }}
                        >
                          {STOP_STATUSES.map((st) => (
                            <option key={st} value={st}>
                              {stopStatusLabel(st)}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td style={{padding: '10px 8px'}}>{s.expectedCapacity ?? '—'}</td>
                      <td style={{padding: '10px 8px'}}>
                        <input
                          type="number"
                          inputMode="numeric"
                          value={draft.actualCapacity}
                          onChange={(e) => updateDraft(s.id, {actualCapacity: e.target.value})}
                          disabled={routeIsReadOnly}
                          style={{
                            width: 110,
                            padding: '8px 10px',
                            borderRadius: 10,
                            border: '1px solid rgba(255,255,255,0.12)',
                            background: 'rgba(255,255,255,0.06)',
                            color: '#f8fafc',
                          }}
                        />
                      </td>
                      <td style={{padding: '10px 8px'}}>
                        <input
                          type="text"
                          value={draft.note}
                          onChange={(e) => updateDraft(s.id, {note: e.target.value})}
                          disabled={routeIsReadOnly}
                          style={{
                            width: '100%',
                            minWidth: 200,
                            padding: '8px 10px',
                            borderRadius: 10,
                            border: '1px solid rgba(255,255,255,0.12)',
                            background: 'rgba(255,255,255,0.06)',
                            color: '#f8fafc',
                          }}
                        />
                      </td>
                      <td style={{padding: '10px 8px', textAlign: 'right'}}>
                        <button
                          type="button"
                          className="button button--sm"
                          onClick={() => handleSave(s.id)}
                          disabled={routeIsReadOnly || savingStopId === s.id}
                        >
                          {savingStopId === s.id ? 'Сохраняем…' : 'Сохранить'}
                        </button>
                      </td>
                      <td style={{padding: '10px 8px', textAlign: 'right'}}>
                        {!routeIsReadOnly && !isTerminal ? (
                          <button
                            type="button"
                            className="button button--danger button--sm"
                            onClick={() => openIncidentModal(s)}
                          >
                            Проблема
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : route && !loading ? (
          <p className="panel__body">В маршруте нет остановок.</p>
        ) : null}

        {!routeIsReadOnly ? (
          <div style={{marginTop: 18, display: 'flex', justifyContent: 'flex-end'}}>
            <button type="button" className="button" onClick={handleFinish} disabled={finishing || loading}>
              {finishing ? 'Завершаем…' : 'Завершить маршрут'}
            </button>
          </div>
        ) : null}
      </section>

      {/* Incident Modal */}
      {incidentModal.open ? (
        <div className="modal-overlay" onClick={closeIncidentModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal__header">
              <h2 className="modal__title">Сообщить о проблеме</h2>
              <p style={{margin: '8px 0 0', color: '#94a3b8', fontSize: 14}}>
                Остановка: {incidentModal.stopAddress}
              </p>
            </div>
            <div className="modal__body">
              {incidentError ? (
                <p style={{color: '#fca5a5', marginBottom: 16}}>{incidentError}</p>
              ) : null}

              <div className="field" style={{marginBottom: 16}}>
                <label className="field__label">Тип проблемы</label>
                <div className="incident-types">
                  {INCIDENT_TYPES.map((t) => (
                    <button
                      key={t.value}
                      type="button"
                      className={`incident-type ${incidentType === t.value ? 'incident-type--selected' : ''}`}
                      onClick={() => setIncidentType(t.value)}
                    >
                      <span className="incident-type__icon">{t.icon}</span>
                      <span>{t.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="field">
                <label className="field__label">Описание (опционально)</label>
                <textarea
                  className="field__textarea"
                  placeholder="Опишите проблему подробнее..."
                  value={incidentDescription}
                  onChange={(e) => setIncidentDescription(e.target.value)}
                />
              </div>
            </div>
            <div className="modal__footer">
              <button
                type="button"
                className="button button--ghost"
                onClick={closeIncidentModal}
                disabled={incidentSubmitting}
              >
                Отмена
              </button>
              <button
                type="button"
                className="button button--danger"
                onClick={handleIncidentSubmit}
                disabled={incidentSubmitting || !incidentType}
              >
                {incidentSubmitting ? 'Отправляем…' : 'Отправить'}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  )
}

export default RoutePage
