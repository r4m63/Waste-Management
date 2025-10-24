-- Роли пользователей
CREATE TYPE user_role AS ENUM ('resident','admin','courier','worker');

-- Пользователи
CREATE TABLE users (
  id          SERIAL PRIMARY KEY,
  role        user_role NOT NULL,
  phone       text UNIQUE,
  name        text,
  is_active   boolean NOT NULL DEFAULT true,
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- Точки сбора
CREATE TABLE garbage_points (
  id              SERIAL PRIMARY KEY,
  address         text NOT NULL,
  capacity        integer NOT NULL CHECK (capacity >= 0),
  is_open         boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now(),
  admin_id        REFERENCES users(id)
);

-- Размеры контейнеров
CREATE TYPE container_size_code AS ENUM ('XS','S','M','L','XL','XXL','XXXL');

-- Заказы/сессии киоска (один контейнер = одна запись)
CREATE TABLE kiosk_orders (
  id                 SERIAL PRIMARY KEY,
  garbage_point_id   bigint NOT NULL REFERENCES garbage_points(id) ON DELETE RESTRICT,
  garbage_container_size  container_size_code NOT NULL,
  user_id            bigint REFERENCES users(id) ON DELETE SET NULL,
  fraction           text NOT NULL,
  created_at         timestamptz NOT NULL DEFAULT now(),
);



-- 2 бизнес процесс

-- Транспорт
CREATE TABLE vehicles (
  id            BIGSERIAL PRIMARY KEY,
  plate_number  text UNIQUE NOT NULL,
  name          text,
  capacity      integer,
  is_active     boolean NOT NULL DEFAULT true
);

-- Смена водителя
CREATE TYPE shift_status AS ENUM ('open','closed');

CREATE TABLE driver_shifts (
  id              BIGSERIAL PRIMARY KEY,
  driver_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  vehicle_id      BIGINT REFERENCES vehicles(id) ON DELETE RESTRICT,
  opened_at       timestamptz NOT NULL DEFAULT now(),
  closed_at       timestamptz,
  status          shift_status NOT NULL DEFAULT 'open'
);

-- Маршрут/рейс
CREATE TYPE route_status AS ENUM ('planned','in_progress','completed','cancelled');

CREATE TABLE routes (
  id                BIGSERIAL PRIMARY KEY,
  planned_date      date NOT NULL,                       -- плановый день
  driver_id         BIGINT REFERENCES users(id),         -- можно NULL до назначения
  vehicle_id        BIGINT REFERENCES vehicles(id),
  shift_id          BIGINT REFERENCES driver_shifts(id), -- связываем с открытой сменой
  planned_start_at  timestamptz,
  planned_end_at    timestamptz,
  started_at        timestamptz,
  finished_at       timestamptz,
  status            route_status NOT NULL DEFAULT 'planned'
);

-- Точки в маршруте (площадка или произвольный адрес)
CREATE TYPE stop_status AS ENUM ('planned','enroute','arrived','loading','unloading','done','skipped','unavailable');

CREATE TABLE route_stops (
  id                BIGSERIAL PRIMARY KEY,
  route_id          BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  seq_no            integer NOT NULL,                    -- порядок
  garbage_point_id  BIGINT REFERENCES garbage_points(id) ON DELETE RESTRICT,
  address           text,                                -- если нет garbage_point_id
  time_from         timestamptz,                         -- окно времени (начало)
  time_to           timestamptz,                         -- окно времени (конец)
  expected_units    integer,                             -- план (в ед.)
  status            stop_status NOT NULL DEFAULT 'planned',
  actual_units      integer,                             -- факт (в ед.)
  note              text
);

-- События по точке (для таймлинии и аналитики)
CREATE TYPE stop_event_type AS ENUM ('start','arrived','loading','unloading','done','skipped','unavailable','comment');

CREATE TABLE stop_events (
  id          BIGSERIAL PRIMARY KEY,
  stop_id     BIGINT NOT NULL REFERENCES route_stops(id) ON DELETE CASCADE,
  event_type  stop_event_type NOT NULL,
  occurred_at timestamptz NOT NULL DEFAULT now(),
  lat         double precision,
  lon         double precision,
  photo_url   text,
  comment     text
);

-- Подтверждение клиента (код/подпись)
CREATE TYPE confirm_method AS ENUM ('code','signature');
-- TO DELETE
CREATE TABLE stop_confirmations (
  id            BIGSERIAL PRIMARY KEY,
  stop_id       BIGINT NOT NULL REFERENCES route_stops(id) ON DELETE CASCADE,
  method        confirm_method NOT NULL,
  code_value    text,                -- при методе 'code'
  signed_by     text,                -- ФИО или идентификатор
  confirmed_at  timestamptz NOT NULL DEFAULT now()
);

-- Инциденты
CREATE TYPE incident_type AS ENUM ('access_denied','traffic','vehicle_issue','overload','other');

CREATE TABLE incidents (
  id          BIGSERIAL PRIMARY KEY,
  route_id    BIGINT REFERENCES routes(id) ON DELETE CASCADE, -- TO DELETE
  stop_id     BIGINT REFERENCES route_stops(id) ON DELETE CASCADE,
  type        incident_type NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  description text,
  photo_url   text,
  resolved    boolean NOT NULL DEFAULT false
);






