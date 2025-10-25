CREATE TYPE user_role          AS ENUM ('resident','admin','courier','worker');
CREATE TYPE order_status       AS ENUM ('created','confirmed','cancelled');
CREATE TYPE container_size_code AS ENUM ('XS','S','M','L','XL','XXL','XXXL');
CREATE TYPE shift_status       AS ENUM ('open','closed');
CREATE TYPE route_status       AS ENUM ('planned','in_progress','completed','cancelled');
CREATE TYPE stop_status        AS ENUM ('planned','enroute','arrived','loading','unloading','done','skipped','unavailable');
CREATE TYPE stop_event_type    AS ENUM ('start','arrived','loading','unloading','done','skipped','unavailable','comment');
CREATE TYPE incident_type      AS ENUM ('access_denied','traffic','vehicle_issue','overload','other');

CREATE TABLE users (
  id          SERIAL PRIMARY KEY,
  role        user_role NOT NULL,
  phone       text,
  name        text,
  is_active   boolean NOT NULL DEFAULT true,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE garbage_points (
  id              SERIAL PRIMARY KEY,
  address         text NOT NULL,
  capacity        integer NOT NULL CHECK (capacity >= 0),
  is_open         boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now(),
  admin_id        integer REFERENCES users(id)
);

CREATE TABLE container_sizes (
  id                  SERIAL PRIMARY KEY,
  code                container_size_code UNIQUE NOT NULL,
  units_per_container integer NOT NULL CHECK (units_per_container > 0)
);

INSERT INTO container_sizes (code, units_per_container) VALUES
('XS',1),('S',2),('M',3),('L',4),('XL',6),('XXL',8),('XXXL',12);

CREATE TABLE fractions (
  id    SERIAL PRIMARY KEY,
  name  text UNIQUE NOT NULL
);

CREATE TABLE kiosk_orders (
  id                 SERIAL PRIMARY KEY,
  garbage_point_id   integer NOT NULL REFERENCES garbage_points(id) ON DELETE RESTRICT,
  container_size_id  integer NOT NULL REFERENCES container_sizes(id) ON DELETE RESTRICT,
  user_id            integer REFERENCES users(id) ON DELETE SET NULL,
  fraction_id        integer NOT NULL REFERENCES fractions(id) ON DELETE RESTRICT,
  created_at         timestamptz NOT NULL DEFAULT now(),
  status             order_status NOT NULL DEFAULT 'confirmed'
);

CREATE INDEX idx_kiosk_orders_point_time ON kiosk_orders (garbage_point_id, created_at DESC);
CREATE INDEX idx_kiosk_orders_user_time  ON kiosk_orders (user_id, created_at DESC);
CREATE INDEX idx_kiosk_orders_fraction   ON kiosk_orders (fraction_id);

CREATE TABLE vehicles (
  id            SERIAL PRIMARY KEY,
  plate_number  text UNIQUE NOT NULL,
  name          text,
  capacity      integer,   -- в кг
  is_active     boolean NOT NULL DEFAULT true
);

CREATE TABLE driver_shifts (
  id              SERIAL PRIMARY KEY,
  driver_id       integer NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  vehicle_id      integer REFERENCES vehicles(id) ON DELETE RESTRICT,
  opened_at       timestamptz NOT NULL DEFAULT now(),
  closed_at       timestamptz,
  status          shift_status NOT NULL DEFAULT 'open',
  CONSTRAINT ck_shift_times CHECK (
    (status='open'   AND closed_at IS NULL) OR
    (status='closed' AND closed_at IS NOT NULL)
  )
);

CREATE UNIQUE INDEX ux_open_shift_per_driver
  ON driver_shifts(driver_id)
  WHERE status = 'open';

CREATE TABLE routes (
  id                SERIAL PRIMARY KEY,
  planned_date      date NOT NULL,
  driver_id         integer REFERENCES users(id),
  vehicle_id        integer REFERENCES vehicles(id),
  shift_id          integer REFERENCES driver_shifts(id),
  planned_start_at  timestamptz,
  planned_end_at    timestamptz,
  started_at        timestamptz,
  finished_at       timestamptz,
  status            route_status NOT NULL DEFAULT 'planned'
);

CREATE TABLE route_stops (
  id                SERIAL PRIMARY KEY,
  route_id          integer NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  seq_no            integer NOT NULL,
  garbage_point_id  integer REFERENCES garbage_points(id) ON DELETE RESTRICT,
  address           text,
  time_from         timestamptz,
  time_to           timestamptz,
  expected_units    integer,
  actual_units      integer,
  status            stop_status NOT NULL DEFAULT 'planned',
  note              text,
  CONSTRAINT uq_route_seq UNIQUE (route_id, seq_no),
  CONSTRAINT ck_seq_positive CHECK (seq_no >= 1),
  CONSTRAINT ck_stop_target CHECK (
    (garbage_point_id IS NOT NULL AND (address IS NULL OR address = ''))
    OR
    (garbage_point_id IS NULL AND address IS NOT NULL AND address <> '')
  ),
  CONSTRAINT ck_units_nonneg CHECK (
    (expected_units IS NULL OR expected_units >= 0) AND
    (actual_units   IS NULL OR actual_units   >= 0)
  ),
  CONSTRAINT ck_time_window CHECK (
    (time_from IS NULL AND time_to IS NULL) OR
    (time_from IS NOT NULL AND time_to IS NOT NULL AND time_from <= time_to)
  )
);

CREATE OR REPLACE FUNCTION route_stops_autoseq()
RETURNS trigger AS $$
BEGIN
  IF NEW.seq_no IS NULL THEN
    SELECT COALESCE(MAX(rs.seq_no),0)+1 INTO NEW.seq_no
    FROM route_stops rs
    WHERE rs.route_id = NEW.route_id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_route_stops_autoseq
BEFORE INSERT ON route_stops
FOR EACH ROW
EXECUTE FUNCTION route_stops_autoseq();

CREATE TABLE stop_events (
  id          SERIAL PRIMARY KEY,
  stop_id     integer NOT NULL REFERENCES route_stops(id) ON DELETE CASCADE,
  event_type  stop_event_type NOT NULL,   -- должен содержать 'comment'
  occurred_at timestamptz NOT NULL DEFAULT now(),
  lat         double precision,
  lon         double precision,
  photo_url   text,
  comment     text
);

CREATE INDEX idx_stop_events_stop_time ON stop_events (stop_id, occurred_at);

CREATE TABLE incidents (
  id           SERIAL PRIMARY KEY,
  stop_id      integer NOT NULL REFERENCES route_stops(id) ON DELETE CASCADE,
  type         incident_type NOT NULL,
  description  text,
  photo_url    text,
  created_by   integer REFERENCES users(id),
  created_at   timestamptz NOT NULL DEFAULT now(),
  updated_at   timestamptz NOT NULL DEFAULT now(),
  resolved     boolean NOT NULL DEFAULT false,
  resolved_at  timestamptz
);

CREATE INDEX idx_incidents_stop      ON incidents (stop_id);
CREATE INDEX idx_incidents_resolved  ON incidents (resolved, created_at DESC);

CREATE OR REPLACE FUNCTION incidents_touch()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  IF NEW.resolved = true AND (OLD.resolved IS DISTINCT FROM NEW.resolved) AND NEW.resolved_at IS NULL THEN
    NEW.resolved_at := now();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_incidents_touch
BEFORE UPDATE ON incidents
FOR EACH ROW
EXECUTE FUNCTION incidents_touch();

CREATE OR REPLACE FUNCTION incidents_after_insert()
RETURNS trigger AS $$
BEGIN
  INSERT INTO stop_events (stop_id, event_type, comment, photo_url)
  VALUES (NEW.stop_id, 'comment',
          'INCIDENT ' || NEW.type || COALESCE(' — ' || NEW.description, ''),
          NEW.photo_url);

  UPDATE route_stops
     SET status = 'unavailable'
   WHERE id = NEW.stop_id
     AND status NOT IN ('done','skipped','unavailable');

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_incidents_ins
AFTER INSERT ON incidents
FOR EACH ROW
EXECUTE FUNCTION incidents_after_insert();

CREATE OR REPLACE FUNCTION incidents_after_update()
RETURNS trigger AS $$
BEGIN
  IF (OLD.resolved IS DISTINCT FROM NEW.resolved) THEN
    INSERT INTO stop_events (stop_id, event_type, comment)
    VALUES (
      NEW.stop_id,
      'comment',
      CASE
        WHEN NEW.resolved THEN 'INCIDENT resolved' || CASE WHEN NEW.description IS NOT NULL THEN ': '||NEW.description ELSE '' END
        ELSE 'INCIDENT reopened'
      END
    );
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_incidents_upd
AFTER UPDATE ON incidents
FOR EACH ROW
EXECUTE FUNCTION incidents_after_update();

CREATE OR REPLACE FUNCTION routes_validate_start()
RETURNS trigger AS $$
DECLARE
  open_shift_id integer;
  open_cnt integer;
BEGIN
  IF NEW.status = 'in_progress' AND (OLD.status IS DISTINCT FROM NEW.status) THEN
    IF NEW.shift_id IS NULL THEN
      RAISE EXCEPTION 'Route %: shift_id is required to start', NEW.id;
    END IF;
    IF NEW.driver_id IS NULL THEN
      RAISE EXCEPTION 'Route %: driver_id is required to start', NEW.id;
    END IF;

    SELECT COUNT(*) INTO open_cnt
    FROM driver_shifts
    WHERE driver_id = NEW.driver_id AND status = 'open';

    IF open_cnt <> 1 THEN
      RAISE EXCEPTION 'Route %: driver % must have exactly one OPEN shift (found: %)',
        NEW.id, NEW.driver_id, open_cnt;
    END IF;

    SELECT id INTO open_shift_id
    FROM driver_shifts
    WHERE driver_id = NEW.driver_id AND status = 'open'
    LIMIT 1;

    IF NEW.shift_id <> open_shift_id THEN
      RAISE EXCEPTION 'Route %: shift_id % does not match the driver''s open shift %',
        NEW.id, NEW.shift_id, open_shift_id;
    END IF;

    IF NEW.started_at IS NULL THEN
      NEW.started_at := now();
    END IF;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_routes_validate_start
BEFORE UPDATE ON routes
FOR EACH ROW
EXECUTE FUNCTION routes_validate_start();

CREATE INDEX idx_routes_plan_driver_status ON routes (planned_date, driver_id, status);

