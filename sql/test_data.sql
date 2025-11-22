-- 1. Создание пользователей
INSERT INTO users (role, phone, name, is_active) VALUES
('admin', '+79110000001', 'Администратор Системы', true),
('courier', '+79110000002', 'Иван Петров', true),
('courier', '+79110000003', 'Сергей Сидоров', true),
('worker', '+79110000004', 'Алексей Козлов', true),
('resident', '+79110000005', 'Мария Иванова', true),
('resident', '+79110000006', 'Ольга Смирнова', true);

-- 2. Создание транспортных средств
INSERT INTO vehicles (plate_number, name, capacity, is_active) VALUES
('А123АА77', 'Газель Next', 1500, true),
('В456ВВ77', 'Камаз 6520', 10000, true),
('С789СС77', 'Ford Transit', 2000, true),
('Е000ЕЕ77', 'MAN TGS', 15000, true);

-- 3. Создание фракций (типов мусора)
INSERT INTO fractions (name, code, description, is_hazardous) VALUES
('Пластик', 'plastic', 'Пластиковые отходы', false),
('Стекло', 'glass', 'Стеклянные отходы', false),
('Металл', 'metal', 'Металлические отходы', false),
('Бумага', 'paper', 'Бумажные отходы', false),
('Опасные отходы', 'hazardous', 'Батарейки, лампы', true);

-- 4. Создание мусорных точек
INSERT INTO garbage_points (address, capacity, is_open, lat, lon, admin_id) VALUES
('ул. Ленина, 1', 500, true, 55.7558, 37.6173, 1),
('ул. Пушкина, 15', 800, true, 55.7600, 37.6200, 1),
('пр. Мира, 25', 1200, true, 55.7818, 37.6168, 1),
('ул. Гагарина, 10', 300, false, 55.7234, 37.6012, 1),
('бульвар Космонавтов, 5', 1000, true, 55.7934, 37.6156, 1);

-- 5. Связи точек и фракций
INSERT INTO garbage_point_fractions (garbage_point_id, fraction_id, is_active) VALUES
(1, 1, true), (1, 2, true), (1, 3, true),
(2, 1, true), (2, 4, true),
(3, 1, true), (3, 2, true), (3, 3, true), (3, 4, true),
(4, 1, true),
(5, 1, true), (5, 2, true), (5, 5, true);

-- 6. Заказы из киосков
INSERT INTO kiosk_orders (garbage_point_id, container_size_id, user_id, fraction_id, status, created_at) VALUES
(1, 2, 5, 1, 'confirmed', NOW() - INTERVAL '2 days'),
(1, 3, 5, 2, 'confirmed', NOW() - INTERVAL '1 day'),
(2, 4, 6, 4, 'confirmed', NOW() - INTERVAL '3 days'),
(3, 1, 5, 1, 'confirmed', NOW()),
(5, 3, 6, 5, 'confirmed', NOW() - INTERVAL '5 hours');

-- 7. Смены водителей
INSERT INTO driver_shifts (driver_id, vehicle_id, opened_at, status) VALUES
(2, 1, NOW() - INTERVAL '3 hours', 'open'),
(3, 2, NOW() - INTERVAL '1 day', 'closed'),
(3, 2, NOW() - INTERVAL '2 hours', 'open');

-- 8. Обновление закрытой смены
UPDATE driver_shifts SET
    closed_at = NOW() - INTERVAL '30 minutes',
    status = 'closed'
WHERE id = 2;

-- 9. Маршруты
INSERT INTO routes (planned_date, driver_id, vehicle_id, shift_id, planned_start_at, planned_end_at, status) VALUES
(CURRENT_DATE, 2, 1, 1, NOW() - INTERVAL '2 hours', NOW() + INTERVAL '2 hours', 'planned'),
(CURRENT_DATE + 1, 3, 2, 3, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 4 hours', 'planned'),
(CURRENT_DATE - 1, 3, 2, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '20 hours', 'completed');

-- 10. Остановки маршрутов
INSERT INTO route_stops (route_id, garbage_point_id, seq_no, time_from, time_to, expected_capacity, status) VALUES
(1, 1, 1, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '30 minutes', 150, 'planned'),
(1, 2, 2, NOW() - INTERVAL '20 minutes', NOW() + INTERVAL '10 minutes', 200, 'planned'),
(1, 3, 3, NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '1 hour', 300, 'planned'),
(2, 4, 1, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 30 minutes', 100, 'planned'),
(2, 5, 2, NOW() + INTERVAL '1 day 1 hour', NOW() + INTERVAL '1 day 2 hours', 250, 'planned'),
(3, 1, 1, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours', 180, 'done'),
(3, 3, 2, NOW() - INTERVAL '22 hours', NOW() - INTERVAL '21 hours', 220, 'done');

-- 11. События остановок
INSERT INTO stop_events (stop_id, event_type, created_at, comment) VALUES
(6, 'arrived', NOW() - INTERVAL '1 day', 'Прибыл на точку'),
(6, 'loading', NOW() - INTERVAL '1 day' + INTERVAL '5 minutes', 'Начало загрузки'),
(6, 'unloading', NOW() - INTERVAL '1 day' + INTERVAL '15 minutes', 'Выгрузка в контейнер'),
(6, 'done', NOW() - INTERVAL '1 day' + INTERVAL '25 minutes', 'Завершено, объем: 170 кг'),
(7, 'arrived', NOW() - INTERVAL '22 hours', 'Прибыл на вторую точку'),
(7, 'loading', NOW() - INTERVAL '22 hours' + INTERVAL '5 minutes', 'Загрузка металла'),
(7, 'done', NOW() - INTERVAL '22 hours' + INTERVAL '20 minutes', 'Завершено, объем: 210 кг');

-- 12. Инциденты
INSERT INTO incidents (stop_id, type, description, created_by, resolved) VALUES
(1, 'access_denied', 'Въезд на территорию закрыт', 2, false),
(4, 'vehicle_issue', 'Прокол колеса, требуется замена', 3, true),
(2, 'overload', 'Контейнер переполнен, не помещается весь объем', 2, false);

-- 13. Обновление фактической вместимости для выполненных остановок
UPDATE route_stops SET actual_capacity = 170 WHERE id = 6;
UPDATE route_stops SET actual_capacity = 210 WHERE id = 7;

-- 14. Обновление маршрута в процессе выполнения
UPDATE routes SET
    status = 'in_progress',
    started_at = NOW() - INTERVAL '2 hours'
WHERE id = 1;

-- 15. Обновление завершенного маршрута
UPDATE routes SET
    finished_at = NOW() - INTERVAL '20 hours'
WHERE id = 3;