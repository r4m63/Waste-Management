-- Тестовые данные для системы управления отходами
BEGIN;

-- Пользователи (пароли: aa для админов, dd для водителей, rr для жителей, ww для работников)
-- BCrypt хеш для "aa": $2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZhQpjppr5FlpIcGdOuBIejUvNO
-- BCrypt хеш для "dd": $2a$10$hBn7RxqbpX3hGqnEfG0M1.gRbS7T5c0KsZLF4pZ8wSVYHZ5bLBvUq
-- BCrypt хеш для "rr": $2a$10$FxmJ8SJXkF5T5LG5ZQqNKuW9F4i5YK8hS5KsJFQb8JxJh5xLGvDXq
-- BCrypt хеш для "ww": $2a$10$HkN7pJ9kQF5bL7cFdGqEQOB8Z5nF6gK9bQ7hJ8cF5gH7qJ8kL9pNq
INSERT INTO users (id, role, phone, name, is_active, login, password) VALUES
(1, 'admin', '+79991234567', 'Иванов Иван', true, 'admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZhQpjppr5FlpIcGdOuBIejUvNO'),
(2, 'admin', '+79991234568', 'Петрова Мария', true, 'admin2', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZhQpjppr5FlpIcGdOuBIejUvNO'),
(3, 'courier', '+79991234569', 'Сидоров Петр', true, 'driver1', '$2a$10$hBn7RxqbpX3hGqnEfG0M1.gRbS7T5c0KsZLF4pZ8wSVYHZ5bLBvUq'),
(4, 'courier', '+79991234570', 'Козлов Андрей', true, 'driver2', '$2a$10$hBn7RxqbpX3hGqnEfG0M1.gRbS7T5c0KsZLF4pZ8wSVYHZ5bLBvUq'),
(5, 'courier', '+79991234571', 'Смирнова Ольга', true, 'driver3', '$2a$10$hBn7RxqbpX3hGqnEfG0M1.gRbS7T5c0KsZLF4pZ8wSVYHZ5bLBvUq'),
(6, 'resident', '+79991234572', 'Васильев Василий', true, 'resident1', '$2a$10$FxmJ8SJXkF5T5LG5ZQqNKuW9F4i5YK8hS5KsJFQb8JxJh5xLGvDXq'),
(7, 'resident', '+79991234573', 'Николаева Анна', true, 'resident2', '$2a$10$FxmJ8SJXkF5T5LG5ZQqNKuW9F4i5YK8hS5KsJFQb8JxJh5xLGvDXq'),
(8, 'resident', '+79991234574', 'Федоров Дмитрий', true, 'resident3', '$2a$10$FxmJ8SJXkF5T5LG5ZQqNKuW9F4i5YK8hS5KsJFQb8JxJh5xLGvDXq'),
(9, 'worker', '+79991234575', 'Михайлов Михаил', true, 'worker1', '$2a$10$HkN7pJ9kQF5bL7cFdGqEQOB8Z5nF6gK9bQ7hJ8cF5gH7qJ8kL9pNq'),
(10, 'worker', '+79991234576', 'Александрова Елена', true, 'worker2', '$2a$10$HkN7pJ9kQF5bL7cFdGqEQOB8Z5nF6gK9bQ7hJ8cF5gH7qJ8kL9pNq');
-- Обновляем sequence для users
SELECT setval('users_id_seq', 10);

-- Точки сбора мусора
INSERT INTO garbage_points (id, address, capacity, is_open, lat, lon, admin_id) VALUES
(1, 'Кронверкский проспект, 49', 100, true, 59.9570, 30.3082, 1),
(2, 'Биржевая площадь, 4', 150, true, 59.9433, 30.3234, 1),
(3, 'Садовая улица, 21', 120, true, 59.9276, 30.3163, 2),
(4, 'Невский проспект, 85', 200, true, 59.9311, 30.3609, 2),
(5, 'Литейный проспект, 55', 180, true, 59.9420, 30.3542, 1),
(6, 'Московский проспект, 100', 250, true, 59.8985, 30.3199, 2),
(7, 'проспект Обуховской Обороны, 120', 150, true, 59.8770, 30.4534, 1),
(8, 'Лиговский проспект, 50', 170, true, 59.9236, 30.3598, 2);
-- Обновляем sequence для garbage_points
SELECT setval('garbage_points_id_seq', 8);

-- Фракции отходов
INSERT INTO fractions (id, name, code, description, is_hazardous) VALUES
(1, 'Пластик', 'plastic', 'ПЭТ бутылки, пластиковые контейнеры', false),
(2, 'Стекло', 'glass', 'Стеклянная тара', false),
(3, 'Бумага', 'paper', 'Картон, газеты, офисная бумага', false),
(4, 'Металл', 'metal', 'Алюминиевые и жестяные банки', false),
(5, 'Батарейки', 'batteries', 'Использованные батарейки и аккумуляторы', true),
(6, 'Электроника', 'electronics', 'Старая электроника и бытовая техника', true),
(7, 'Органика', 'organic', 'Пищевые отходы', false),
(8, 'Текстиль', 'textile', 'Старая одежда и ткани', false);
SELECT setval('fractions_id_seq', 8);

-- Связи фракций с точками (все точки принимают основные фракции)
INSERT INTO garbage_point_fractions (garbage_point_id, fraction_id, is_active) VALUES
-- Точка 1: Кронверкский (все виды кроме электроники)
(1, 1, true), (1, 2, true), (1, 3, true), (1, 4, true), (1, 5, true), (1, 7, true), (1, 8, true),
-- Точка 2: Биржевая (все виды)
(2, 1, true), (2, 2, true), (2, 3, true), (2, 4, true), (2, 5, true), (2, 6, true), (2, 7, true), (2, 8, true),
-- Точка 3: Садовая (только безопасные)
(3, 1, true), (3, 2, true), (3, 3, true), (3, 4, true), (3, 7, true), (3, 8, true),
-- Точка 4: Невский (все виды)
(4, 1, true), (4, 2, true), (4, 3, true), (4, 4, true), (4, 5, true), (4, 6, true), (4, 7, true), (4, 8, true),
-- Точка 5: Литейный (основные + батарейки)
(5, 1, true), (5, 2, true), (5, 3, true), (5, 4, true), (5, 5, true), (5, 7, true),
-- Точка 6: Московский (все виды)
(6, 1, true), (6, 2, true), (6, 3, true), (6, 4, true), (6, 5, true), (6, 6, true), (6, 7, true), (6, 8, true),
-- Точка 7: Обуховской (только безопасные)
(7, 1, true), (7, 2, true), (7, 3, true), (7, 4, true), (7, 7, true),
-- Точка 8: Лиговский (основные)
(8, 1, true), (8, 2, true), (8, 3, true), (8, 4, true), (8, 7, true), (8, 8, true);

-- Заказы киосков  
INSERT INTO kiosk_orders (id, garbage_point_id, container_size_id, user_id, fraction_id, status) VALUES
-- Точка 1: Кронверкский
(1, 1, 1, 6, 1, 'confirmed'), -- XS пластик
(2, 1, 3, 7, 3, 'confirmed'), -- M бумага
(3, 1, 5, 8, 2, 'confirmed'), -- XL стекло
(4, 1, 2, 6, 4, 'confirmed'), -- S металл
-- Точка 2: Биржевая
(5, 2, 4, 7, 1, 'confirmed'), -- L пластик
(6, 2, 6, 8, 6, 'confirmed'), -- XXL электроника
(7, 2, 3, 6, 3, 'confirmed'), -- M бумага
-- Точка 3: Садовая
(8, 3, 2, 7, 1, 'confirmed'), -- S пластик
(9, 3, 4, 8, 2, 'confirmed'), -- L стекло
(10, 3, 3, 6, 7, 'confirmed'), -- M органика
-- Точка 4: Невский
(11, 4, 5, 7, 1, 'confirmed'), -- XL пластик
(12, 4, 6, 8, 3, 'confirmed'), -- XXL бумага
(13, 4, 7, 6, 4, 'confirmed'), -- XXXL металл
(14, 4, 4, 7, 2, 'confirmed'), -- L стекло
-- Точка 5: Литейный
(15, 5, 3, 8, 1, 'confirmed'), -- M пластик
(16, 5, 4, 6, 5, 'confirmed'), -- L батарейки
(17, 5, 2, 7, 3, 'confirmed'), -- S бумага
-- Точка 6: Московский
(18, 6, 6, 8, 1, 'confirmed'), -- XXL пластик
(19, 6, 7, 6, 2, 'confirmed'), -- XXXL стекло
(20, 6, 5, 7, 6, 'confirmed'), -- XL электроника
(21, 6, 4, 8, 3, 'confirmed'); -- L бумага
SELECT setval('kiosk_orders_id_seq', 21);

-- Транспортные средства
INSERT INTO vehicles (id, plate_number, name, capacity, is_active) VALUES
(1, 'A001AA178', 'ГАЗель-Фермер', 1500, true),
(2, 'B002BB178', 'КАМАЗ-5490', 15000, true),
(3, 'C003CC178', 'Mercedes Sprinter', 2000, true),
(4, 'D004DD178', 'Volvo FH16', 20000, true),
(5, 'E005EE178', 'MAN TGX', 18000, true);
SELECT setval('vehicles_id_seq', 5);

-- Открытые смены водителей
INSERT INTO driver_shifts (id, driver_id, vehicle_id, opened_at, status) VALUES
(1, 3, 1, now() - interval '2 hours', 'open'),
(2, 4, 3, now() - interval '1 hour', 'open');

-- Закрытые смены (история)
INSERT INTO driver_shifts (id, driver_id, vehicle_id, opened_at, closed_at, status) VALUES
(3, 3, 1, now() - interval '2 days', now() - interval '2 days' + interval '8 hours', 'closed'),
(4, 4, 2, now() - interval '2 days', now() - interval '2 days' + interval '9 hours', 'closed'),
(5, 5, 3, now() - interval '1 day', now() - interval '1 day' + interval '7 hours', 'closed');
SELECT setval('driver_shifts_id_seq', 5);

-- Маршруты (запланированные)
INSERT INTO routes (id, planned_date, driver_id, vehicle_id, shift_id, planned_start_at, planned_end_at, status) VALUES
(1, current_date, 3, 1, 1, current_date + interval '8 hours', current_date + interval '16 hours', 'planned'),
(2, current_date, 4, 3, 2, current_date + interval '9 hours', current_date + interval '17 hours', 'planned'),
(3, current_date + interval '1 day', NULL, NULL, NULL, NULL, NULL, 'planned');
SELECT setval('routes_id_seq', 3);

-- Остановки маршрутов
INSERT INTO route_stops (id, route_id, seq_no, garbage_point_id, expected_capacity, status) VALUES
-- Маршрут 1
(1, 1, 1, 1, 80, 'planned'),
(2, 1, 2, 3, 60, 'planned'),
(3, 1, 3, 5, 70, 'planned'),
-- Маршрут 2
(4, 2, 1, 2, 90, 'planned'),
(5, 2, 2, 4, 110, 'planned'),
(6, 2, 3, 6, 150, 'planned'),
-- Маршрут 3 (без водителя)
(7, 3, 1, 7, 100, 'planned'),
(8, 3, 2, 8, 120, 'planned');
SELECT setval('route_stops_id_seq', 8);

COMMIT;

