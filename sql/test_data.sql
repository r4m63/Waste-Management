-- 1. Заполняем пользователей
INSERT INTO users (role, phone, name, is_active)
VALUES ('admin', '+79111111111', 'Александр Иванов', true),
       ('courier', '+79112222222', 'Петр Сидоров', true),
       ('courier', '+79113333333', 'Мария Петрова', true),
       ('worker', '+79114444444', 'Иван Кузнецов', true),
       ('resident', '+79115555555', 'Сергей Смирнов', true),
       ('resident', '+79116666666', 'Ольга Орлова', true);

-- 2. Заполняем точки сбора
INSERT INTO garbage_points (address, capacity, lat, lon, admin_id)
VALUES ('ул. Ленина, 10', 1000, 55.7558, 37.6173, 1),
       ('пр. Мира, 25', 800, 55.7547, 37.6206, 1),
       ('ул. Садовая, 5', 1200, 55.7580, 37.6215, 1),
       ('пер. Бульварный, 15', 600, 55.7572, 37.6159, 1);

-- 3. Заполняем фракции отходов
INSERT INTO fractions (name, code, description, is_hazardous)
VALUES ('Пластик', 'plastic', 'Пластиковые отходы', false),
       ('Стекло', 'glass', 'Стеклянные отходы', false),
       ('Бумага', 'paper', 'Макулатура', false),
       ('Металл', 'metal', 'Металлические отходы', false),
       ('Опасные отходы', 'hazardous', 'Батарейки, лампы', true);

-- 4. Связываем точки с фракциями
INSERT INTO garbage_point_fractions (garbage_point_id, fraction_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 3),
       (2, 4),
       (3, 2),
       (3, 3),
       (3, 4),
       (4, 1),
       (4, 5);

-- 5. Заказы через киоск
INSERT INTO kiosk_orders (garbage_point_id, container_size_id, user_id, fraction_id, status)
VALUES (1, 3, 5, 1, 'confirmed'),
       (1, 2, 6, 2, 'confirmed'),
       (2, 4, 5, 3, 'created'),
       (3, 3, 6, 4, 'confirmed'),
       (4, 1, 5, 1, 'cancelled');

-- 6. Транспортные средства
INSERT INTO vehicles (plate_number, name, capacity, is_active)
VALUES ('А123БВ777', 'ГАЗель NEXT', 3000, true),
       ('Х456УВ777', 'Hyundai HD78', 5000, true),
       ('М789ТР777', 'Ford Transit', 4000, false);

-- 7. Смены водителей
INSERT INTO driver_shifts (driver_id, vehicle_id, opened_at, closed_at, status)
VALUES (2, 1, '2024-06-01 08:00:00', '2024-06-01 17:00:00', 'closed'),
       (3, 2, '2024-06-01 09:00:00', NULL, 'open'),
       (2, 1, '2024-06-02 08:00:00', NULL, 'open');

-- 8. Маршруты
INSERT INTO routes (planned_date, driver_id, vehicle_id, shift_id, planned_start_at, planned_end_at, started_at,
                    finished_at, status)
VALUES ('2024-06-01', 2, 1, 1, '2024-06-01 09:00:00', '2024-06-01 12:00:00', '2024-06-01 09:05:00',
        '2024-06-01 11:45:00', 'completed'),
       ('2024-06-01', 3, 2, 2, '2024-06-01 10:00:00', '2024-06-01 15:00:00', '2024-06-01 10:10:00', NULL,
        'in_progress'),
       ('2024-06-02', 2, 1, 3, '2024-06-02 09:00:00', '2024-06-02 13:00:00', NULL, NULL, 'planned');

-- 9. Остановки маршрутов
INSERT INTO route_stops (route_id, seq_no, garbage_point_id, address, time_from, time_to, expected_capacity,
                         actual_capacity, status, note)
VALUES
-- Для первого (завершенного) маршрута
(1, 1, 1, NULL, '2024-06-01 09:00:00', '2024-06-01 09:30:00', 150, 140, 'done', 'Пластик и стекло'),
(1, 2, 2, NULL, '2024-06-01 10:00:00', '2024-06-01 10:40:00', 200, 180, 'done', 'Бумага'),
(1, 3, NULL, 'ул. Непланируемая, 7', '2024-06-01 11:00:00', '2024-06-01 11:20:00', 100, 100, 'done', 'Временная точка'),

-- Для второго (активного) маршрута
(2, 1, 3, NULL, '2024-06-01 10:00:00', '2024-06-01 10:45:00', 180, 175, 'done', NULL),
(2, 2, 4, NULL, '2024-06-01 11:30:00', '2024-06-01 12:15:00', 120, NULL, 'loading', 'Опасные отходы'),
(2, 3, NULL, 'ул. Строителей, 20/2', '2024-06-01 13:00:00', '2024-06-01 13:40:00', 200, NULL, 'planned', 'Новый район'),

-- Для третьего (запланированного) маршрута
(3, 1, 1, NULL, '2024-06-02 09:00:00', '2024-06-02 09:30:00', 170, NULL, 'planned', NULL),
(3, 2, 3, NULL, '2024-06-02 10:00:00', '2024-06-02 10:45:00', 190, NULL, 'planned', NULL);

-- 10. События остановок
INSERT INTO stop_events (stop_id, event_type, created_at, photo_url, comment)
VALUES
-- Для первой остановки первого маршрута
(1, 'arrived', '2024-06-01 09:02:00', NULL, 'Прибыли к точке'),
(1, 'loading', '2024-06-01 09:15:00', 'photo1.jpg', 'Начало погрузки'),
(1, 'done', '2024-06-01 09:28:00', NULL, 'Погрузка завершена'),

-- Для второй остановки первого маршрута
(2, 'arrived', '2024-06-01 10:03:00', NULL, NULL),
(2, 'loading', '2024-06-01 10:10:00', NULL, 'Бумага прессуется'),
(2, 'done', '2024-06-01 10:38:00', 'photo2.jpg', NULL),

-- Для активной остановки
(5, 'arrived', '2024-06-01 11:29:00', NULL, 'Подъехали к точке'),
(5, 'loading', '2024-06-01 11:35:00', 'photo3.jpg', 'Погрузка опасных отходов');

-- 11. Инциденты
INSERT INTO incidents (stop_id, type, description, photo_url, created_by, resolved)
VALUES (2, 'access_denied', 'Не смогли подъехать из-за припаркованных машин', 'incident1.jpg', 2, true),
       (5, 'vehicle_issue', 'Проблема с гидравликой подъемника', NULL, 3, false),
       (3, 'other', 'Дополнительные отходы не помещаются в контейнер', 'incident2.jpg', 2, true);