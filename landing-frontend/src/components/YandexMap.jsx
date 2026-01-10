import { useEffect, useRef, useState } from 'react'
import { API_URL } from '../lib/config'

export default function YandexMap() {
    const mapRef = useRef(null)
    const mapInstanceRef = useRef(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        if (typeof window.ymaps === 'undefined') {
            return
        }

        const fetchAndDisplayPoints = async () => {
            try {
                const response = await fetch(`${API_URL}/api/garbage-points/open`)
                
                if (!response.ok) {
                    throw new Error('Не удалось загрузить точки приема')
                }

                const points = await response.json()

                window.ymaps.ready(() => {
                    if (!mapRef.current || mapInstanceRef.current) return

                    const center = points.length > 0 && points[0].lat && points[0].lon
                        ? [points[0].lat, points[0].lon]
                        : [55.7558, 37.6173]

                    const map = new window.ymaps.Map(mapRef.current, {
                        center: center,
                        zoom: 11,
                        controls: ['zoomControl', 'fullscreenControl']
                    })

                    mapInstanceRef.current = map

                    const clusterer = new window.ymaps.Clusterer({
                        preset: 'islands#greenClusterIcons',
                        groupByCoordinates: false,
                        clusterDisableClickZoom: true,
                        clusterHideIconOnBalloonOpen: false,
                        geoObjectHideIconOnBalloonOpen: false
                    })

                    points.forEach((point) => {
                        if (point.lat && point.lon) {
                            const placemark = new window.ymaps.Placemark(
                                [point.lat, point.lon],
                                {
                                    balloonContentHeader: `Точка приема №${point.id}`,
                                    balloonContentBody: point.address,
                                    balloonContentFooter: `Вместимость: ${point.capacity}`,
                                    hintContent: point.address
                                },
                                {
                                    preset: 'islands#greenRecyclingIcon',
                                    iconColor: '#4CAF50'
                                }
                            )
                            clusterer.add(placemark)
                        }
                    })

                    map.geoObjects.add(clusterer)
                    setLoading(false)
                })
            } catch (err) {
                setError(err.message)
                setLoading(false)
            }
        }

        fetchAndDisplayPoints()

        return () => {
            if (mapInstanceRef.current) {
                mapInstanceRef.current.destroy()
                mapInstanceRef.current = null
            }
        }
    }, [])

    if (error) {
        return (
            <div style={{ 
                width: '100%', 
                height: '600px', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                background: '#f5f5f5'
            }}>
                <p style={{ color: '#d32f2f' }}>Ошибка загрузки карты: {error}</p>
            </div>
        )
    }

    return (
        <div style={{ position: 'relative' }}>
            <div ref={mapRef} style={{ width: '100%', height: '600px' }} />
            {loading && (
                <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'rgba(255, 255, 255, 0.8)',
                    zIndex: 1000
                }}>
                    <p>Загрузка точек приема...</p>
                </div>
            )}
        </div>
    )
}

