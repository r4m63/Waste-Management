<div align="center">

# ♻️ Waste Management Platform

**Многоуровневая система управления сбором, маршрутизацией и переработкой отходов**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18%2F19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-7-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Gradle](https://img.shields.io/badge/Gradle-Kotlin%20DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

<br/>

*Единая цифровая платформа для администраторов, водителей, киосков и публичной карты точек приема.*

<br/>

[🚀 Возможности](#-возможности) •
[🏗️ Архитектура](#️-архитектура) •
[🛠️ Технологии](#️-технологический-стек) •
[⚡ Быстрый старт](#-быстрый-старт) •
[📡 API](#-api-основные-группы) •
[🧪 Тестирование](#-тестирование)

</div>

---

## 📌 О проекте

`Waste Management` автоматизирует операционный цикл обращения с отходами:
- оформление заявок на вывоз,
- расчет загрузки точек,
- построение и выполнение маршрутов,
- управление сменами водителей,
- обработку инцидентов,
- контроль состояния инфраструктуры через единый backend.

Система ориентирована на эксплуатационную прозрачность, снижение ручной диспетчеризации и ускорение принятия решений.

---

## 🚀 Возможности

<table>
<tr>
<td width="50%">

### 🧭 Маршрутизация
- Автогенерация маршрутов по активным заказам
- Назначение водителя и планового окна
- Старт/финиш маршрута с проверками статусов
- Обновление остановок в реальном времени

</td>
<td width="50%">

### 🚛 Смены и транспорт
- Открытие/закрытие смен водителей
- Контроль активных маршрутов при закрытии
- Привязка транспорта к смене
- Проверка незавершенных остановок

</td>
</tr>
<tr>
<td width="50%">

### 🧪 Операции киоска
- Создание и обновление заявок на вывоз
- Валидация фракций и точек приема
- Работа с контейнерами и весом
- Поддержка ролей `KIOSK` и `ADMIN`

</td>
<td width="50%">

### ⚠️ Инциденты и контроль
- Создание инцидентов на остановках
- Резолв инцидентов с фиксацией времени
- Централизованная обработка ошибок API
- Ролевой доступ по Spring Security

</td>
</tr>
</table>

---

## 🧩 Приложения в составе системы

| Приложение | Назначение | Стек | Порт dev |
|:--|:--|:--|:--:|
| `admin-frontend` | Панель администратора | React, Vite, Tailwind, shadcn/ui, AG Grid | `21001` |
| `driver-frontend` | Кабинет водителя | React, Vite, React Router | `21002` |
| `kiosk-frontend` | Интерфейс киоска | React, Vite, NextUI, Tailwind | `21003` |
| `landing-frontend` | Публичная карта точек приема | React, Vite, Yandex Maps | `21004` |
| `backend` | Бизнес-логика и API | Spring Boot, JPA, Security, PostgreSQL | см. `SERVER_PORT` |

---

## 🏗️ Архитектура

```text
Frontend Apps (Admin / Driver / Kiosk / Landing)
            |
            | HTTP + Cookie Session
            v
 Spring Boot Backend (Controller -> Service -> Repository)
            |
            | JPA / SQL Functions
            v
        PostgreSQL
```

### Роли и доступ
- `ADMIN`: полный доступ к управлению справочниками, маршрутами, персоналом, инцидентами.
- `DRIVER`: свои маршруты, смены, операции по остановкам, создание инцидентов.
- `KIOSK`: создание/ведение заявок, доступ к точкам/фракциям/контейнерам.
- Публичный доступ: вход в систему и просмотр открытых точек приема.

---

## 🛠️ Технологический стек

### Backend
- Java 21
- Spring Boot (`web`, `validation`, `security`, `data-jpa`)
- PostgreSQL 16
- Lombok
- Gradle Kotlin DSL

### Frontend
- React 18/19
- Vite 7
- React Router 7
- Tailwind CSS
- Radix UI / shadcn
- AG Grid
- NextUI
- Yandex Maps

### Infrastructure
- Docker Compose
- SQL schema + stored procedures/functions (`sql/init.sql`)
- UML и проектная документация (`docs/`)

---

## 📂 Структура проекта

```text
Waste-Management/
├── src/                          # Java backend
│   ├── main/java/ru/itmo/wastemanagement/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── exception/
│   │   └── util/
│   └── test/java/ru/itmo/wastemanagement/
├── sql/                          # init.sql / drop.sql
├── admin-frontend/
├── driver-frontend/
├── kiosk-frontend/
├── landing-frontend/
├── docs/
├── docker-compose.yml
├── build.gradle.kts
└── README.md
```

---

## ⚡ Быстрый старт

### 1) Требования

- Java 21
- Node.js 20+
- npm 10+
- Docker + Docker Compose
- `psql`

### 2) Настройка env-файлов

```bash
cp .env.sample .env
cp admin-frontend/.env.example admin-frontend/.env
cp driver-frontend/.env.example driver-frontend/.env
cp kiosk-frontend/.env.example kiosk-frontend/.env
cp landing-frontend/.env.sample landing-frontend/.env
```

### 3) Запуск PostgreSQL

```bash
docker compose up -d pg
```

### 4) Инициализация БД

```bash
psql -h localhost -p 5432 -U sb_user -d db_name -f sql/init.sql
```

### 5) Запуск backend

```bash
./gradlew bootRun
```

### 6) Запуск frontend-приложений

```bash
cd admin-frontend && npm install && npm run dev
cd driver-frontend && npm install && npm run dev
cd kiosk-frontend && npm install && npm run dev
cd landing-frontend && npm install && npm run dev
```

---

## 🔧 Конфигурация

### Backend `.env`
- `DB_URL`, `DB_USER`, `DB_PASS`
- `SERVER_PORT`
- `APP_URL_ADMIN_FRONTEND`
- `APP_URL_LANDING_FRONTEND`
- `APP_URL_KIOSK_FRONTEND`
- `APP_URL_DRIVER_FRONTEND`
- `APP_URL_BACKEND`

### Frontend `.env`
- `admin/driver/kiosk`: `VITE_BACKEND_BASE`
- `landing`: `VITE_API_URL`

> Рекомендация: держите порты backend и landing разными, чтобы избежать конфликта локального запуска.

---

## 📡 API (основные группы)

<details>
<summary><b>Auth</b> — аутентификация и сессия</summary>

| Method | Endpoint | Description |
|:--:|:--|:--|
| `POST` | `/login` | Вход пользователя |
| `POST` | `/logout` | Выход и очистка сессии |
| `GET` | `/me` | Текущий пользователь |

</details>

<details>
<summary><b>Routes</b> — маршруты и остановки</summary>

| Method | Endpoint |
|:--:|:--|
| `GET` | `/api/routes` |
| `GET` | `/api/routes/my` |
| `GET` | `/api/routes/{id}/my` |
| `POST` | `/api/routes/auto-generate` |
| `PUT` | `/api/routes/{id}/assign` |
| `PUT` | `/api/routes/{id}/start` |
| `PUT` | `/api/routes/{id}/finish` |
| `PUT` | `/api/routes/{routeId}/stops/{stopId}` |
| `DELETE` | `/api/routes/{id}` |

</details>

<details>
<summary><b>Shifts / Incidents / Master Data</b></summary>

| Group | Prefix |
|:--|:--|
| Смены водителей | `/api/shifts` |
| Инциденты | `/api/incidents` |
| Точки сбора | `/api/garbage-points` |
| Заявки киосков | `/api/kiosk-orders` |
| Контейнеры | `/api/container-sizes` |
| Фракции | `/api/fractions` |
| Водители | `/api/drivers` |
| Транспорт | `/api/vehicles` |
| Киоски | `/api/kiosk` |

</details>

---

## 🧪 Тестирование

Запуск тестов backend:

```bash
./gradlew test
```

В проекте покрыты:
- сервисный слой,
- контроллеры,
- security и exception handling,
- DTO mapping,
- utility-компоненты.

---

## 📚 Документация

Каталог `docs/` содержит:
- use-case, activity и sequence-диаграммы,
- UML диаграммы архитектуры, классов, пакетов и deployment,
- дополнительные материалы проектной документации.

---

## ✅ Engineering Practices

- Слоистая архитектура (`controller -> service -> repository`)
- Транзакционность в сервисах (`@Transactional`)
- Валидация DTO через `jakarta.validation`
- Централизованный `GlobalExceptionHandler`
- Ролевая безопасность на уровне маршрутов API
- SQL-ограничения целостности и процедурная логика в БД

---

<div align="center">

**Waste Management** • профессиональная платформа для операционного управления обращением с отходами

</div>
