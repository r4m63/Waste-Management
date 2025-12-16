BEGIN;

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
    kiosk_id   integer     REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE container_sizes
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        text UNIQUE NOT NULL,
    capacity    integer     NOT NULL CHECK (capacity > 0),
    length      double precision,
    width       double precision,
    height      double precision,
    description text,
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE fractions
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         text UNIQUE NOT NULL,
    code         text UNIQUE NOT NULL,
    description  text,
    is_hazardous boolean     NOT NULL DEFAULT false,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

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
    container_size_id BIGINT      NOT NULL REFERENCES container_sizes (id) ON DELETE RESTRICT,
    user_id           integer     REFERENCES users (id) ON DELETE SET NULL,
    fraction_id       integer     NOT NULL REFERENCES fractions (id) ON DELETE RESTRICT,
    weight            double precision,
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
    driver_id  BIGINT      NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    vehicle_id BIGINT      REFERENCES vehicles (id) ON DELETE SET NULL,
    opened_at  timestamptz NOT NULL                                      DEFAULT now(),
    closed_at  timestamptz,
    status     text        NOT NULL CHECK (status IN ('open', 'closed')) DEFAULT 'open',
    CHECK (
        (status = 'open' AND closed_at IS NULL) OR
        (status = 'closed' AND closed_at IS NOT NULL)
        ),
    CHECK (closed_at IS NULL OR closed_at >= opened_at)
);

CREATE UNIQUE INDEX driver_shifts_one_open_per_driver
    ON driver_shifts (driver_id)
    WHERE status = 'open';

CREATE TABLE routes
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    planned_date     date    NOT NULL,
    driver_id        integer REFERENCES users (id) ON DELETE SET NULL,
    vehicle_id       integer REFERENCES vehicles (id) ON DELETE SET NULL,
    shift_id         integer REFERENCES driver_shifts (id) ON DELETE SET NULL,
    planned_start_at timestamptz,
    planned_end_at   timestamptz,
    started_at       timestamptz,
    finished_at      timestamptz,
    status           text    NOT NULL DEFAULT 'planned' CHECK (status IN ('planned', 'in_progress', 'completed', 'cancelled'))
);

CREATE TABLE route_stops
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id          integer NOT NULL REFERENCES routes (id) ON DELETE CASCADE,
    seq_no            integer NOT NULL,
    garbage_point_id  integer REFERENCES garbage_points (id) ON DELETE SET NULL,
    address           text,
    time_from         timestamptz,
    time_to           timestamptz,
    expected_capacity integer,
    actual_capacity   integer,
    status            text    NOT NULL DEFAULT 'planned' CHECK (status IN ('planned', 'enroute', 'arrived', 'loading',
                                                                           'unloading', 'done', 'skipped',
                                                                           'unavailable')),
    note              text
);

CREATE TABLE stop_events
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stop_id    integer     NOT NULL REFERENCES route_stops (id) ON DELETE CASCADE,
    event_type text        NOT NULL CHECK (event_type IN ('start', 'arrived', 'loading', 'unloading', 'done', 'skipped',
                                                          'unavailable', 'comment')),
    created_at timestamptz NOT NULL DEFAULT now(),
    photo_url  text,
    comment    text
);

CREATE TABLE incidents
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stop_id     integer     NOT NULL REFERENCES route_stops (id) ON DELETE CASCADE,
    type        text        NOT NULL CHECK (type IN ('access_denied', 'traffic', 'vehicle_issue', 'overload', 'other')),
    description text,
    photo_url   text,
    created_by  integer     REFERENCES users (id) ON DELETE SET NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    resolved    boolean     NOT NULL DEFAULT false,
    resolved_at timestamptz
);

COMMIT;
