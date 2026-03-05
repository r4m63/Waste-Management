package ru.itmo.wastemanagement.util;

import java.beans.Expression;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class GridTablePredicateBuilder {

    // "filterModel": {
    // "type": {
    // "filterType": "text",
    // "type": "equals",
    // "filter": "CAR"
    // },
    // "enginePower": {
    // "filterType": "number",
    // "type": "inRange",
    // "filter": 100,
    // "filterTo": 200
    // },
    // "creationTime": {
    // "filterType": "date",
    // "type": "inRange",
    // "dateFrom": "2025-10-01",
    // "dateTo": "2025-10-20"
    // },
    // "fuelType": {
    // "filterType": "set",
    // "values": ["KEROSENE","NUCLEAR"]
    // }
    // }

    public static List<Predicate> build(CriteriaBuilder cb, Root<?> root, Map<String, Object> filterModel) {
        List<Predicate> out = new ArrayList<>();

        if (filterModel == null || filterModel.isEmpty())
            return out;

        for (var entry : filterModel.entrySet()) {
            String col = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) entry.getValue();
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

    public static Path<?> resolvePath(Root<?> root, String colId) {

        if (colId == null || colId.isBlank())
            return root.get("id");

        String[] parts = colId.split("\\.");

        Path<?> p = root;
        From<?, ?> from = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            boolean isLast = (i == parts.length - 1);
            if (!isLast) {
                Join<?, ?> existing = null;
                for (Join<?, ?> j : from.getJoins()) {
                    if (j.getAttribute() != null && j.getAttribute().getName().equals(part)) {
                        existing = j;
                        break;
                    }
                }

                if (existing != null) {
                    from = existing;
                    p = from;
                    continue;
                }

                try {
                    from = from.join(part, JoinType.LEFT);
                    p = from;
                    continue;
                } catch (IllegalArgumentException ignored) {
                }
            }

            p = p.get(part);

            if (p instanceof From<?, ?> f)
                from = f;
        }

        return p;
    }

    private static void handleText(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        String type = (String) fm.get("type");
        String val = (String) fm.get("filter");
        if (val == null || val.isBlank())
            return;

        Expression<String> exp = cb.lower(path.as(String.class));
        String p = val.toLowerCase(Locale.ROOT);

        switch (type) {
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

        Class<?> jt = path.getJavaType();

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
                    f1 != null ? new BigDecimal(f1.toString()) : null,
                    f2 != null ? new BigDecimal(f2.toString()) : null);
        }
    }

    private static <T extends Number & Comparable<T>> void addNumber(
            CriteriaBuilder cb, List<Predicate> out, String type,
            Expression<T> num, T v1, T v2) {
        if (v1 == null && !"inRange".equals(type))
            return;

        switch (type) {
            case "equals" -> out.add(cb.equal(num, v1));
            case "notEqual" -> out.add(cb.notEqual(num, v1));
            case "lessThan" -> out.add(cb.lessThan(num, v1));
            case "lessThanOrEqual" -> out.add(cb.lessThanOrEqualTo(num, v1));
            case "greaterThan" -> out.add(cb.greaterThan(num, v1));
            case "greaterThanOrEqual" -> out.add(cb.greaterThanOrEqualTo(num, v1));
            case "inRange" -> {
                if (v1 != null && v2 != null) {
                    out.add(cb.and(cb.greaterThanOrEqualTo(num, v1), cb.lessThanOrEqualTo(num, v2)));
                } else if (v1 != null) {
                    out.add(cb.greaterThanOrEqualTo(num, v1));
                } else if (v2 != null) {
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
                if (d2 == null)
                    d2 = d1;
                LocalDateTime end = d2.plusDays(1).atStartOfDay();
                out.add(cb.between(dt, start, end));
            }
            default -> {
            }
        }
    }

    private static void handleSet(CriteriaBuilder cb, List<Predicate> out, Path<?> path, Map<String, Object> fm) {
        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) fm.get("values");
        if (values == null || values.isEmpty())
            return;

        CriteriaBuilder.In<Object> in = cb.in(path);
        for (Object v : values)
            in.value(castForPath(path, v));
        out.add(in);
    }

    @SuppressWarnings("unchecked")
    private static Object castForPath(Path<?> path, Object value) {
        Class<?> t = path.getJavaType();

        if (value != null && t.isInstance(value))
            return value;

        String stringValue = value == null ? null : value.toString();

        if (t.equals(Boolean.class) || t.equals(Boolean.TYPE)) {
            if (value instanceof Boolean)
                return value;
            return Boolean.valueOf(stringValue);
        }

        if (t.isEnum())
            return Enum.valueOf((Class<Enum>) t, stringValue);
        if (t.equals(Integer.class) || t.equals(Integer.TYPE))
            return Integer.valueOf(stringValue);
        if (t.equals(Long.class) || t.equals(Long.TYPE))
            return Long.valueOf(stringValue);
        if (t.equals(Double.class) || t.equals(Double.TYPE))
            return Double.valueOf(stringValue);
        if (t.equals(Float.class) || t.equals(Float.TYPE))
            return Float.valueOf(stringValue);
        if (t.equals(BigDecimal.class))
            return new BigDecimal(stringValue);
        return stringValue;
    }

    private static Number toNumber(Object o) {
        if (o == null)
            return null;
        if (o instanceof Number n)
            return n;
        return new BigDecimal(o.toString());
    }

    public static LocalDate parseToLocalDate(String s) {
        if (s == null || s.isBlank())
            return null;
        return LocalDate.parse(s);
    }
}
