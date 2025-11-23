CREATE TYPE user_role AS ENUM ('resident','admin','courier','worker');
CREATE TYPE order_status AS ENUM ('created','confirmed','cancelled');
CREATE TYPE container_size_code AS ENUM ('XS','S','M','L','XL','XXL','XXXL');
CREATE TYPE shift_status AS ENUM ('open','closed');
CREATE TYPE route_status AS ENUM ('planned','in_progress','completed','cancelled');
CREATE TYPE stop_status AS ENUM ('planned','enroute','arrived','loading','unloading','done','skipped','unavailable');
CREATE TYPE stop_event_type AS ENUM ('start','arrived','loading','unloading','done','skipped','unavailable','comment');
CREATE TYPE incident_type AS ENUM ('access_denied','traffic','vehicle_issue','overload','other');


CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    role       user_role   NOT NULL,
    phone      text UNIQUE,
    name       text,
    is_active  boolean     NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON users (role);

CREATE TABLE garbage_points
(
    id         SERIAL PRIMARY KEY,
    address    text        NOT NULL,
    capacity   integer     NOT NULL CHECK (capacity >= 0),
    is_open    boolean     NOT NULL DEFAULT true,
    lat        double precision,
    lon        double precision,
    created_at timestamptz NOT NULL DEFAULT now(),
    admin_id   integer     REFERENCES users (id) ON DELETE SET NULL,
    CHECK (lat IS NULL OR (lat >= -90 AND lat <= 90)),
    CHECK (lon IS NULL OR (lon >= -180 AND lon <= 180))
);
CREATE INDEX ON garbage_points (is_open);


CREATE TABLE container_sizes
(
    id       SERIAL PRIMARY KEY,
    code     container_size_code UNIQUE NOT NULL,
    capacity integer                    NOT NULL CHECK (capacity > 0)
    -- еще длина/ширина/высота
);

INSERT INTO container_sizes (code, capacity)
VALUES ('XS', 1),
       ('S', 2),
       ('M', 3),
       ('L', 4),
       ('XL', 6),
       ('XXL', 8),
       ('XXXL', 12)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE fractions
(
    id           SERIAL PRIMARY KEY,
    name         text UNIQUE NOT NULL, -- "Пластик", "Стекло"
    code         text UNIQUE NOT NULL, -- "plastic", "glass"
    description  text,
    is_hazardous boolean     NOT NULL DEFAULT false,
    -- garbage classification code
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ON fractions (code);

-- M:N: какие фракции принимаются на каких точках
CREATE TABLE garbage_point_fractions
(
    garbage_point_id integer NOT NULL REFERENCES garbage_points (id) ON DELETE CASCADE,
    fraction_id      integer NOT NULL REFERENCES fractions (id) ON DELETE RESTRICT,
    is_active        boolean NOT NULL DEFAULT true,
    PRIMARY KEY (garbage_point_id, fraction_id)
);
CREATE INDEX ON garbage_point_fractions (fraction_id);


CREATE TABLE kiosk_orders
(
    id                SERIAL PRIMARY KEY,
    garbage_point_id  integer      NOT NULL REFERENCES garbage_points (id) ON DELETE RESTRICT,
    container_size_id integer      NOT NULL REFERENCES container_sizes (id) ON DELETE RESTRICT,
    user_id           integer      REFERENCES users (id) ON DELETE SET NULL,
    fraction_id       integer      NOT NULL REFERENCES fractions (id) ON DELETE RESTRICT,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    status            order_status NOT NULL DEFAULT 'confirmed'
);
CREATE INDEX ON kiosk_orders (garbage_point_id, created_at DESC);
CREATE INDEX ON kiosk_orders (user_id, created_at DESC);
CREATE INDEX ON kiosk_orders (fraction_id);
CREATE INDEX ON kiosk_orders (container_size_id);


CREATE TABLE vehicles
(
    id           SERIAL PRIMARY KEY,
    plate_number text UNIQUE NOT NULL,
    name         text,
    capacity     integer,
    is_active    boolean     NOT NULL DEFAULT true
);

CREATE TABLE driver_shifts
(
    id         SERIAL PRIMARY KEY,
    driver_id  integer      NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    vehicle_id integer      REFERENCES vehicles (id) ON DELETE SET NULL,
    opened_at  timestamptz  NOT NULL DEFAULT now(),
    closed_at  timestamptz,
    status     shift_status NOT NULL DEFAULT 'open',
    -- Если смена открыта - closed_at должен быть NULL
    -- Если смена закрыта - closed_at должен быть заполнен
    CHECK (
        (status = 'open' AND closed_at IS NULL) OR
        (status = 'closed' AND closed_at IS NOT NULL)
        ),
    -- Если смена закрыта - время закрытия не может быть раньше времени открытия
    CHECK (closed_at IS NULL OR closed_at >= opened_at)
);
-- гарантирует, что у одного водителя может быть только одна открытая смена
CREATE UNIQUE INDEX ON driver_shifts (driver_id) WHERE status = 'open';
CREATE INDEX ON driver_shifts (driver_id);
CREATE INDEX ON driver_shifts (vehicle_id);

CREATE TABLE routes
(
    id               SERIAL PRIMARY KEY,
    planned_date     date         NOT NULL,
    driver_id        integer      REFERENCES users (id) ON DELETE SET NULL,
    vehicle_id       integer      REFERENCES vehicles (id) ON DELETE SET NULL,
    shift_id         integer      REFERENCES driver_shifts (id) ON DELETE SET NULL,
    planned_start_at timestamptz,
    planned_end_at   timestamptz,
    started_at       timestamptz,
    finished_at      timestamptz,
    status           route_status NOT NULL DEFAULT 'planned'
);

CREATE INDEX ON routes (planned_date, driver_id, status);
CREATE INDEX ON routes (vehicle_id);
CREATE INDEX ON routes (shift_id);

CREATE TABLE route_stops
(
    id                SERIAL PRIMARY KEY,
    route_id          integer     NOT NULL REFERENCES routes (id) ON DELETE CASCADE,
    seq_no            integer     NOT NULL,
    garbage_point_id  integer     REFERENCES garbage_points (id) ON DELETE SET NULL,
    address           text,
    time_from         timestamptz,
    time_to           timestamptz,
    expected_capacity integer,
    actual_capacity   integer,
    status            stop_status NOT NULL DEFAULT 'planned',
    note              text,
    -- В рамках одного маршрута порядковые номера остановок должны быть уникальными
    UNIQUE (route_id, seq_no),
    -- Нумерация остановок начинается с 1
    CHECK (seq_no >= 1),
    -- Остановка должна быть:
    -- либо по точке на карте: garbage_point_id заполнен, address пустой
    -- либо по адресу: garbage_point_id пустой, address заполнен
    -- Не может быть: Одновременно и точка и адрес, Ни точки ни адреса
    CHECK (
        (garbage_point_id IS NOT NULL AND (address IS NULL OR address = ''))
            OR
        (garbage_point_id IS NULL AND address IS NOT NULL AND address <> '')
        ),
    -- Оба поля вместимости могут быть NULL, но если указаны - должны быть > 0
    CHECK (
        (expected_capacity IS NULL OR expected_capacity >= 0) AND
        (actual_capacity IS NULL OR actual_capacity >= 0)
        ),
    -- Временное окно может быть:
    -- либо Не указано вообще (оба поля NULL)
    -- либо Указано полностью (оба поля заполнены, причем time_from ≤ time_to)
    -- Не может быть: Указано только одно из времен time_from позже чем time_to
    CHECK (
        (time_from IS NULL AND time_to IS NULL) OR
        (time_from IS NOT NULL AND time_to IS NOT NULL AND time_from <= time_to)
        )
);

CREATE INDEX ON route_stops (route_id);
CREATE INDEX ON route_stops (route_id, status);
CREATE INDEX ON route_stops (garbage_point_id);

-- Триггер автонумерации seq_no в рамках одного маршрута
CREATE OR REPLACE FUNCTION route_stops_autoseq()
    RETURNS trigger AS
$$
BEGIN
    -- Если seq_no не указан явно
    IF NEW.seq_no IS NULL THEN
        -- Находим максимальный существующий seq_no для этого маршрута
        -- Находит максимальный порядковый номер среди существующих остановок маршрута, если нет то 0
        SELECT COALESCE(MAX(rs.seq_no), 0) + 1
        INTO NEW.seq_no
        FROM route_stops rs
        WHERE rs.route_id = NEW.route_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_route_stops_autoseq
    BEFORE INSERT -- Перед вставкой новой записи в route_stops
    ON route_stops
    FOR EACH ROW -- Для каждой строки
EXECUTE FUNCTION route_stops_autoseq();


CREATE TABLE stop_events
(
    id         SERIAL PRIMARY KEY,
    stop_id    integer         NOT NULL REFERENCES route_stops (id) ON DELETE CASCADE,
    event_type stop_event_type NOT NULL,
    created_at timestamptz     NOT NULL DEFAULT now(),
    photo_url  text,
    comment    text
);
CREATE INDEX ON stop_events (stop_id, created_at);

CREATE TABLE incidents
(
    id          SERIAL PRIMARY KEY,
    stop_id     integer       NOT NULL REFERENCES route_stops (id) ON DELETE CASCADE,
    type        incident_type NOT NULL,
    description text,
    photo_url   text,
    created_by  integer       REFERENCES users (id) ON DELETE SET NULL,
    created_at  timestamptz   NOT NULL DEFAULT now(),
    updated_at  timestamptz   NOT NULL DEFAULT now(),
    resolved    boolean       NOT NULL DEFAULT false,
    resolved_at timestamptz
);
CREATE INDEX ON incidents (stop_id);
CREATE INDEX ON incidents (resolved, created_at DESC);
CREATE INDEX ON incidents (type);
CREATE INDEX ON incidents (created_by);


