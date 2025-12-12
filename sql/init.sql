BEGIN;
-- CREATE TYPE user_role AS ENUM ('RESIDENT','ADMIN','DRIVER','KIOSK');
-- CREATE TYPE order_status AS ENUM ('CREATED','CONFIRMED','CANCELLED');
CREATE TYPE container_size_code AS ENUM ('XS','S','M','L','XL','XXL','XXXL');
CREATE TYPE shift_status AS ENUM ('open','closed');
CREATE TYPE route_status AS ENUM ('planned','in_progress','completed','cancelled');
CREATE TYPE stop_status AS ENUM ('planned','enroute','arrived','loading','unloading','done','skipped','unavailable');
CREATE TYPE stop_event_type AS ENUM ('start','arrived','loading','unloading','done','skipped','unavailable','comment');
CREATE TYPE incident_type AS ENUM ('access_denied','traffic','vehicle_issue','overload','other');


CREATE TABLE users
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role       text        NOT NULL CHECK (role IN ('RESIDENT', 'ADMIN', 'DRIVER', 'KIOSK')),
    phone      text UNIQUE,
    name       text,
    is_active  boolean     NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    login      text UNIQUE,
    password   text
);

CREATE TABLE garbage_points
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    address    text        NOT NULL,
    capacity   integer     NOT NULL CHECK (capacity >= 0),
    is_open    boolean     NOT NULL DEFAULT true,
    lat        double precision,
    lon        double precision,
    created_at timestamptz NOT NULL DEFAULT now(),
    admin_id   integer     REFERENCES users (id) ON DELETE SET NULL,
    kiosk_id   integer     REFERENCES users (id) ON DELETE SET NULL,
    CHECK (lat IS NULL OR (lat >= -90 AND lat <= 90)),
    CHECK (lon IS NULL OR (lon >= -180 AND lon <= 180))
);

CREATE TABLE container_sizes
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         text UNIQUE NOT NULL, -- "Пластик", "Стекло"
    code         text UNIQUE NOT NULL, -- "plastic", "glass"
    description  text,
    is_hazardous boolean     NOT NULL DEFAULT false,
    -- garbage classification code
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

-- M:N: какие фракции принимаются на каких точках
CREATE TABLE garbage_point_fractions
(
    garbage_point_id integer NOT NULL REFERENCES garbage_points (id) ON DELETE CASCADE,
    fraction_id      integer NOT NULL REFERENCES fractions (id) ON DELETE RESTRICT,
    is_active        boolean NOT NULL DEFAULT true,
    PRIMARY KEY (garbage_point_id, fraction_id)
);

CREATE TABLE kiosk_orders
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    garbage_point_id  integer     NOT NULL REFERENCES garbage_points (id) ON DELETE RESTRICT,
    container_size_id integer     NOT NULL REFERENCES container_sizes (id) ON DELETE RESTRICT,
    user_id           integer     REFERENCES users (id) ON DELETE SET NULL,
    fraction_id       integer     NOT NULL REFERENCES fractions (id) ON DELETE RESTRICT,
    created_at        timestamptz NOT NULL DEFAULT now(),
    status            text        NOT NULL CHECK (status IN ('CREATED', 'CONFIRMED', 'CANCELLED'))
);

CREATE TABLE vehicles
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plate_number text UNIQUE NOT NULL,
    name         text,
    capacity     integer,
    is_active    boolean     NOT NULL DEFAULT true,
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE driver_shifts
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    driver_id  BIGINT       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    vehicle_id BIGINT       REFERENCES vehicles (id) ON DELETE SET NULL,
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

-- у одного водителя может быть только одна открытая смена
CREATE UNIQUE INDEX driver_shifts_one_open_per_driver ON driver_shifts (driver_id) WHERE status = 'open';


CREATE TABLE routes
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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

CREATE TABLE route_stops
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stop_id    integer         NOT NULL REFERENCES route_stops (id) ON DELETE CASCADE,
    event_type stop_event_type NOT NULL,
    created_at timestamptz     NOT NULL DEFAULT now(),
    photo_url  text,
    comment    text
);

CREATE TABLE incidents
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
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

-- Пользователи: фильтрация по ролям (админка, поиск курьеров)
CREATE INDEX ON users (role);

-- Точки сбора: показ только открытых точек на карте
CREATE INDEX ON garbage_points (is_open);

-- Фракции: быстрый поиск по коду (API, валидация)
CREATE INDEX ON fractions (code);

-- Связи точек и фракций: поиск точек для конкретной фракции
CREATE INDEX ON garbage_point_fractions (fraction_id);

-- Заказы: история заказов пользователя (личный кабинет)
CREATE INDEX ON kiosk_orders (user_id, created_at DESC);

-- Заказы: аналитика по типам отходов
CREATE INDEX ON kiosk_orders (fraction_id);

-- Заказы: планирование загрузки по размерам контейнеров
CREATE INDEX ON kiosk_orders (container_size_id);

-- Смены: отслеживание использования транспорта
CREATE INDEX ON driver_shifts (vehicle_id);

-- Маршруты: ежедневное планирование и мониторинг выполнения
CREATE INDEX ON routes (planned_date, driver_id, status);

-- Маршруты: загрузка автомобилей, история использования
CREATE INDEX ON routes (vehicle_id);

-- Маршруты: связь со сменами водителей
CREATE INDEX ON routes (shift_id);

-- Остановки: мониторинг прогресса маршрута в реальном времени
CREATE INDEX ON route_stops (route_id, status);

-- Остановки: история посещений и аналитика точек сбора
CREATE INDEX ON route_stops (garbage_point_id);

-- События: хронология операций на остановках (отчеты, аудит)
CREATE INDEX ON stop_events (stop_id, created_at);

-- Инциденты: все проблемы по конкретной остановке
CREATE INDEX ON incidents (stop_id);

-- Инциденты: панель управления (новые нерешенные сначала)
CREATE INDEX ON incidents (resolved, created_at DESC);

-- Инциденты: аналитика по типам проблем
CREATE INDEX ON incidents (type);

-- Инциденты: отслеживание активности сотрудников
CREATE INDEX ON incidents (created_by);

-- Создание заказа через киоск с валидацией
CREATE OR REPLACE FUNCTION create_kiosk_order(
    p_garbage_point_id INTEGER,
    p_container_size_id INTEGER,
    p_user_id INTEGER,
    p_fraction_id INTEGER
) RETURNS INTEGER AS
$$
DECLARE
    v_order_id        INTEGER;
    v_point_open      BOOLEAN;
    v_fraction_active BOOLEAN;
BEGIN
    -- Валидация точки сбора
    -- Проверяем существует ли точка и открыта ли она для приема отходов
    SELECT is_open
    INTO v_point_open
    FROM garbage_points
    WHERE id = p_garbage_point_id;

    -- Если точка не найдена или закрыта - отказываем в создании заказа
    IF NOT FOUND OR NOT v_point_open THEN
        RAISE EXCEPTION 'Garbage point not found or closed';
    END IF;

    -- Валидация фракции для точки
    -- Проверяем принимает ли данная точка указанный тип отходов
    -- Используем EXISTS для быстрой проверки наличия связи в garbage_point_fractions
    SELECT EXISTS (SELECT 1
                   FROM garbage_point_fractions
                   WHERE garbage_point_id = p_garbage_point_id
                     AND fraction_id = p_fraction_id
                     AND is_active = true -- Проверяем что фракция активна для точки
    )
    INTO v_fraction_active;

    -- Если точка не принимает данный тип отходов - бросаем исключение
    IF NOT v_fraction_active THEN
        RAISE EXCEPTION 'Fraction not accepted at this point';
    END IF;

    -- Создание заказа
    -- Все проверки пройдены, создаем заказ со статусом 'confirmed'
    INSERT INTO kiosk_orders
        (garbage_point_id, container_size_id, user_id, fraction_id, status)
    VALUES (p_garbage_point_id, p_container_size_id, p_user_id, p_fraction_id, 'confirmed')
    RETURNING id INTO v_order_id;
    -- Получаем ID созданного заказа

    -- Возвращаем ID нового заказа для дальнейшего использования
    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql;

-- Открытие/закрытие смены водителя (ИСПРАВЛЕННАЯ ВЕРСИЯ)
CREATE OR REPLACE PROCEDURE manage_driver_shift(
    p_driver_id INTEGER,
    p_action TEXT, -- 'open' or 'close' - теперь второй параметр
    p_vehicle_id INTEGER DEFAULT NULL -- параметр с default всегда последний
) AS
$$
DECLARE
    v_shift_id      INTEGER;
    v_open_shift_id INTEGER;
BEGIN
    -- Обработка действия "open" - открытие новой смены
    IF p_action = 'open' THEN
        -- Проверяем нет ли уже открытой смены у этого водителя
        SELECT id
        INTO v_open_shift_id
        FROM driver_shifts
        WHERE driver_id = p_driver_id
          AND status = 'open';

        -- Если нашли открытую смену - бросаем исключение
        IF FOUND THEN
            RAISE EXCEPTION 'Driver already has open shift: %', v_open_shift_id;
        END IF;

        -- Создаем новую смену
        INSERT INTO driver_shifts (driver_id, vehicle_id)
        VALUES (p_driver_id, p_vehicle_id)
        RETURNING id INTO v_shift_id;

        -- Обработка действия "close" - закрытие текущей смены
    ELSIF p_action = 'close' THEN
        -- Находим открытую смену водителя
        SELECT id
        INTO v_shift_id
        FROM driver_shifts
        WHERE driver_id = p_driver_id
          AND status = 'open';

        -- Валидация: проверяем что смена для закрытия существует
        IF NOT FOUND THEN
            RAISE EXCEPTION 'No open shift found for driver';
        END IF;

        -- Закрываем смену
        UPDATE driver_shifts
        SET status    = 'closed',
            closed_at = now()
        WHERE id = v_shift_id;

        -- Обработка невалидного действия
    ELSE
        RAISE EXCEPTION 'Invalid action: %. Use ''open'' or ''close''', p_action;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Начать маршрут с проверками
CREATE OR REPLACE FUNCTION start_route(
    p_route_id INTEGER,
    p_driver_id INTEGER
) RETURNS BOOLEAN AS
$$
DECLARE
    v_route_status route_status;
    v_shift_id     INTEGER;
    v_vehicle_id   INTEGER;
BEGIN
    -- Получаем текущий статус маршрута
    -- Проверяем существует ли маршрут и получаем его текущее состояние
    SELECT status, shift_id, vehicle_id
    INTO v_route_status, v_shift_id, v_vehicle_id
    FROM routes
    WHERE id = p_route_id;

    -- Если маршрут не найден - прерываем выполнение
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Route not found';
    END IF;

    -- Валидация: можно начинать только запланированные маршруты
    -- Защита от повторного запуска или запуска в неправильном статусе
    IF v_route_status != 'planned' THEN
        RAISE EXCEPTION 'Route cannot be started. Current status: %', v_route_status;
    END IF;

    -- Проверяем открытую смену водителя
    -- Убеждаемся что водитель имеет активную смену для работы
    SELECT id
    INTO v_shift_id
    FROM driver_shifts
    WHERE driver_id = p_driver_id
      AND status = 'open';

    -- Если у водителя нет открытой смены - нельзя начать маршрут
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Driver has no open shift';
    END IF;

    -- Обновляем маршрут - переводим в статус "в процессе"
    -- Фиксируем время начала, назначаем водителя и технику
    UPDATE routes
    SET status     = 'in_progress', -- Меняем статус на "в процессе"
        started_at = now(),         -- Фиксируем фактическое время начала
        driver_id  = p_driver_id,   -- Назначаем водителя
        shift_id   = v_shift_id,    -- Связываем с текущей сменой
        -- COALESCE: используем уже назначенный vehicle_id или берем из смены водителя
        vehicle_id = COALESCE(v_vehicle_id, (SELECT vehicle_id FROM driver_shifts WHERE id = v_shift_id))
    WHERE id = p_route_id;

    -- Создаем события для всех остановок маршрута
    -- Инициируем события "start" для каждой запланированной остановки
    INSERT INTO stop_events (stop_id, event_type)
    SELECT id, 'start'
    FROM route_stops
    WHERE route_id = p_route_id
      AND status = 'planned';

    -- Возвращаем успешный результат
    -- TRUE сигнализирует что операция выполнена без ошибок
    RETURN true;
END;
$$ LANGUAGE plpgsql;
COMMIT;