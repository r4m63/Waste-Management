package ru.itmo.wastemanagement.util;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@UtilityClass
public final class GridTablePredicateBuilder {

//    "filterModel": {
//        "type": {
//            "filterType": "text",
//            "type": "equals",
//            "filter": "CAR"
//        },
//        "enginePower": {
//            "filterType": "number",
//            "type": "inRange",
//            "filter": 100,
//            "filterTo": 200
//        },
//        "creationTime": {
//            "filterType": "date",
//            "type": "inRange",
//            "dateFrom": "2025-10-01",
//            "dateTo": "2025-10-20"
//        },
//        "fuelType": {
//            "filterType": "set",
//            "values": ["KEROSENE","NUCLEAR"]
//        }
//    }

    //
    public static List<Predicate> build(CriteriaBuilder cb, Root<?> root, Map<String, Object> filterModel) {
        List<Predicate> out = new ArrayList<>(); // все условия (WHERE)

        if (filterModel == null || filterModel.isEmpty())
            return out; // никаких ограничений и фильтров

        for (var entry : filterModel.entrySet()) { // в массив мап
            String col = entry.getKey(); // colId
            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) entry.getValue(); // сами фильтры внутри colId поля
            String ft = (String) fm.get("filterType");

            Path<?> path = resolvePath(root, col);

            switch (String.valueOf(ft)) {
                case "text" -> handleText(cb, out, path, fm);
                case "number" -> handleNumber(cb, out, path, fm);
                case "date" -> handleDate(cb, out, path, fm);
                case "set" -> handleSet(cb, out, path, fm);
                default -> {
                }
            }
        }
        return out;
    }

    // resolvePath берёт строку вида "admin.id" или "coordinates.x", создаёт нужные JOIN и в конце возвращает Path до
    // конечной колонки, чтобы можно было строить по нему фильтры WHERE/ORDER BY в Criteria API
    public static Path<?> resolvePath(Root<?> root, String colId) {

        // если фронт не прислал имя колонки (или прислал пустое),
        // по умолчанию считаем, что фильтровать/сортировать нужно по "id" корневой сущности
        if (colId == null || colId.isBlank())
            return root.get("id");

        // разбиваем "admin.id" -> ["admin","id"], "coordinates.x" -> ["coordinates","x"]
        String[] parts = colId.split("\\.");

        // p - текущий путь Path от которого будет браться .get("...") к полю
        // from - текущий источник (From), от которого можно делать .join("...")
        // Изначально оба смотрят на корневую таблицу (root)
        Path<?> p = root;
        From<?, ?> from = root;

        // по сегментам пути
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i]; // текущий сегмент пути ("admin" или "id")

            boolean isLast = (i == parts.length - 1); // последний ли это сегмент
            if (!isLast) {
                // если сегмент НЕ последний - это ассоциация (ManyToOne/OneToOne),
                // т.е. нужен JOIN. Но прежде чем создавать новый, попробуем найти существующий.

                Join<?, ?> existing = null;
                for (Join<?, ?> j : from.getJoins()) { // пробегаем все JOIN'ы, уже повешенные на текущий 'from'
                    if (j.getAttribute() != null && j.getAttribute().getName().equals(part)) {
                        existing = j; // нашли join по имени атрибута ("admin")
                        break;
                    }
                }

                if (existing != null) {
                    // если join уже существует - просто переиспользуем его
                    from = existing;  // теперь текущий источник - это найденный join (например, таблица admin)
                    p = from;         // и путь также указывает на этот join
                    continue;         // идём к следующему сегменту
                }

                // Если подходящего join'а ещё нет - пробуем его создать
                try {
                    // from.join(part, LEFT) создаст LEFT JOIN по ассоциации 'part'
                    // (например, LEFT JOIN admin a ON a.id = v.admin_id).
                    from = from.join(part, JoinType.LEFT);
                    p = from; // текущий путь теперь указывает на только что созданный join
                    continue; // идём к следующему сегменту
                } catch (IllegalArgumentException ignored) {
                    // Это fallback: если "part" - не ассоциация (а, скажем, вложенная структура без join),
                    // провайдер может бросить IllegalArgumentException. Тогда JOIN мы не делаем,
                    // и упадём в код ниже (p = p.get(part)) - т.е. просто возьмём под-путь без join
                }
            }

            // Если это ПОСЛЕДНИЙ сегмент (или join не создался), берём под-путь через .get("part"):
            // - если 'p' указывает на сущность/embeddable - это даст колонку (например, "id" или "x")
            // - если 'p' указывает на join - это даст колонку присоединённой таблицы
            p = p.get(part);

            // Иногда p после .get(...) может оказаться чем-то, что тоже является From (редкие кейсы:
            // провайдер трактует вложенную сущность как отдельный источник)
            // Тогда обновим 'from', чтобы следующие циклы могли делать join уже оттуда
            if (p instanceof From<?, ?> f)
                from = f;
        }

        // Возвращаем конечный Path<?> - это «указатель» на нужную колонку (атрибут)
        // Его потом можно использовать в предикатах/сортировках: cb.equal(p, ...), cb.asc(p), и т.д
        return p;
    }

    private static void handleText(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        String type = (String) fm.get("type"); // тип текстового фильтра (filterModel.type.type)
        String val = (String) fm.get("filter"); // значение, что введено в фильтр
        if (val == null || val.isBlank())
            return;

        // регистронезависимое сравнение
        Expression<String> exp = cb.lower(path.as(String.class));
        String p = val.toLowerCase(Locale.ROOT);

        switch (type) {
            // % - вайлдкард подстановочный символ в SQL для оператора LIKE
            case "contains" -> out.add(cb.like(exp, "%" + p + "%"));
            case "equals" -> out.add(cb.equal(exp, p));
            case "startsWith" -> out.add(cb.like(exp, p + "%"));
            case "endsWith" -> out.add(cb.like(exp, "%" + p));
            case "notEqual" -> out.add(cb.notEqual(exp, p));
            default -> {
            }
        }
    }

    private static void handleNumber(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        String type = (String) fm.get("type");
        Number f1 = toNumber(fm.get("filter"));
        Number f2 = toNumber(fm.get("filterTo"));

        Class<?> jt = path.getJavaType(); // реальный Java-тип поля в JPA-модели

        // кастим path к точному типу поля
        // сравнивать Expression<Integer> с Double нельзя
        if (jt == Integer.class || jt == Integer.TYPE) {
            addNumber(cb, out, type, path.as(Integer.class),
                    f1 != null ? f1.intValue() : null,
                    f2 != null ? f2.intValue() : null);
        } else if (jt == Long.class || jt == Long.TYPE) {
            addNumber(cb, out, type, path.as(Long.class),
                    f1 != null ? f1.longValue() : null,
                    f2 != null ? f2.longValue() : null);
        } else if (jt == Float.class || jt == Float.TYPE) {
            addNumber(cb, out, type, path.as(Float.class),
                    f1 != null ? f1.floatValue() : null,
                    f2 != null ? f2.floatValue() : null);
        } else if (jt == Double.class || jt == Double.TYPE) {
            addNumber(cb, out, type, path.as(Double.class),
                    f1 != null ? f1.doubleValue() : null,
                    f2 != null ? f2.doubleValue() : null);
        } else if (jt == BigDecimal.class) {
            addNumber(cb, out, type, path.as(BigDecimal.class),
                    f1 != null ? new BigDecimal(f1.toString()) : null, // toString -- чтобы без погрешности float получить
                    f2 != null ? new BigDecimal(f2.toString()) : null);
        }
    }

    // сборка Predicate под нужную операцию
    private static <T extends Number & Comparable<T>> void addNumber(
            CriteriaBuilder cb, List<Predicate> out, String type,
            Expression<T> num, T v1, T v2
    ) {
        if (v1 == null && !"inRange".equals(type))
            return; // если операция не inRange и нет первого значения - фильтровать нечего

        switch (type) {
            case "equals" -> out.add(cb.equal(num, v1));                            // num =  v1
            case "notEqual" -> out.add(cb.notEqual(num, v1));                       // num <> v1
            case "lessThan" -> out.add(cb.lessThan(num, v1));                       // num <  v1
            case "lessThanOrEqual" -> out.add(cb.lessThanOrEqualTo(num, v1));       // num <= v1
            case "greaterThan" -> out.add(cb.greaterThan(num, v1));                 // num >  v1
            case "greaterThanOrEqual" -> out.add(cb.greaterThanOrEqualTo(num, v1)); // num >= v1
            case "inRange" -> {
                if (v1 != null && v2 != null) {
                    // Полный диапазон [v1; v2]
                    out.add(cb.and(cb.greaterThanOrEqualTo(num, v1), cb.lessThanOrEqualTo(num, v2)));
                } else if (v1 != null) {
                    // только левая граница: [v1; +inf)
                    out.add(cb.greaterThanOrEqualTo(num, v1));
                } else if (v2 != null) {
                    // только правая граница: (-inf; v2]
                    out.add(cb.lessThanOrEqualTo(num, v2));
                }
            }
            default -> {
            }
        }
    }

    private static void handleDate(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        if (!LocalDateTime.class.isAssignableFrom(path.getJavaType()))
            return;

        String type = (String) fm.get("type");
        String d1s = (String) fm.get("dateFrom");
        String d2s = (String) fm.get("dateTo");
        if (d1s == null || d1s.isBlank())
            return;

        LocalDate d1 = parseToLocalDate(d1s);
        if (d1 == null)
            return;

        LocalDateTime start = d1.atStartOfDay();
        Expression<LocalDateTime> dt = path.as(LocalDateTime.class);

        switch (type) {
            case "equals" -> {
                LocalDateTime end = d1.plusDays(1).atStartOfDay();
                out.add(cb.between(dt, start, end));
            }
            case "lessThan" -> out.add(cb.lessThan(dt, start));
            case "greaterThan" -> {
                LocalDateTime end = d1.plusDays(1).atStartOfDay();
                out.add(cb.greaterThanOrEqualTo(dt, end));
            }
            case "inRange" -> {
                LocalDate d2 = parseToLocalDate(d2s);
                if (d2 == null) d2 = d1;
                LocalDateTime end = d2.plusDays(1).atStartOfDay();
                out.add(cb.between(dt, start, end));
            }
            default -> {
            }
        }
    }

    private static void handleSet(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        @SuppressWarnings("unchecked")
        // список значений из фильтра
        // "filterType": "set", "values": ["KEROSENE","NUCLEAR"]
        List<String> values = (List<String>) fm.get("values");
        if (values == null || values.isEmpty())
            return;

        CriteriaBuilder.In<Object> in = cb.in(path); // предикат IN (...) для столбца, на который указывает path.
        for (String v : values)// Каждое входное значение из UI приходит строкой - приводим к типу колонки
            in.value(castForPath(path, v));
        out.add(in);
    }

    @SuppressWarnings("unchecked")
    private static Object castForPath(Path<?> path, String value) {
        Class<?> t = path.getJavaType();
        if (t.isEnum())
            return Enum.valueOf((Class<Enum>) t, value);
        if (t.equals(Integer.class) || t.equals(Integer.TYPE))
            return Integer.valueOf(value);
        if (t.equals(Long.class) || t.equals(Long.TYPE))
            return Long.valueOf(value);
        if (t.equals(Double.class) || t.equals(Double.TYPE))
            return Double.valueOf(value);
        if (t.equals(Float.class) || t.equals(Float.TYPE))
            return Float.valueOf(value);
        if (t.equals(BigDecimal.class))
            return new BigDecimal(value);
        return value;
    }

    private static Number toNumber(Object o) {
        if (o == null)
            return null;
        if (o instanceof Number n)
            return n;
        return new BigDecimal(o.toString());
    }

    public static LocalDate parseToLocalDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s); // "yyyy-MM-dd"
    }
}



