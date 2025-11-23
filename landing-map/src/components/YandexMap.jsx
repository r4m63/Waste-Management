import { useEffect, useRef } from 'react'

export default function YandexMap() {
    const mapRef = useRef(null)
    const mapInstanceRef = useRef(null)

    useEffect(() => {
        if (typeof window.ymaps === 'undefined') {
            return
        }

        window.ymaps.ready(() => {
            if (!mapRef.current || mapInstanceRef.current) return

            // Инициализация карты (центр на Москве, можно изменить)
            const map = new window.ymaps.Map(mapRef.current, {
                center: [55.7558, 37.6173],
                zoom: 11,
                controls: ['zoomControl', 'fullscreenControl']
            })

            mapInstanceRef.current = map

            // Точки приема мусора (пример координат в Москве)
            const collectionPoints = [
                { coords: [55.7558, 37.6173], title: 'Точка приема №1', address: 'ул. Тверская, 1' },
                { coords: [55.7520, 37.6156], title: 'Точка приема №2', address: 'Красная площадь, 1' },
                { coords: [55.7519, 37.6180], title: 'Точка приема №3', address: 'ул. Ильинка, 4' },
                { coords: [55.7495, 37.6135], title: 'Точка приема №4', address: 'ул. Варварка, 10' },
                { coords: [55.7570, 37.6130], title: 'Точка приема №5', address: 'ул. Петровка, 15' },
            ]

            // Создание кластеризатора для точек
            const clusterer = new window.ymaps.Clusterer({
                preset: 'islands#greenClusterIcons',
                groupByCoordinates: false,
                clusterDisableClickZoom: true,
                clusterHideIconOnBalloonOpen: false,
                geoObjectHideIconOnBalloonOpen: false
            })

            // Добавление точек на карту
            collectionPoints.forEach((point) => {
                const placemark = new window.ymaps.Placemark(
                    point.coords,
                    {
                        balloonContentHeader: point.title,
                        balloonContentBody: point.address,
                        balloonContentFooter: 'Режим работы: 24/7',
                        hintContent: point.title
                    },
                    {
                        preset: 'islands#greenRecyclingIcon',
                        iconColor: '#4CAF50'
                    }
                )
                clusterer.add(placemark)
            })

            map.geoObjects.add(clusterer)
        })

        return () => {
            if (mapInstanceRef.current) {
                mapInstanceRef.current.destroy()
                mapInstanceRef.current = null
            }
        }
    }, [])

    return <div ref={mapRef} style={{ width: '100%', height: '600px' }} />
}

