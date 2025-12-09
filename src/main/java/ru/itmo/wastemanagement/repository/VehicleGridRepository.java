package ru.itmo.wastemanagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.Vehicle;
import ru.itmo.wastemanagement.util.GridTablePredicateBuilder;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class VehicleGridRepository {

    @PersistenceContext
    private final EntityManager em;

    public List<Vehicle> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.getEndRow() - req.getStartRow());
        final int offset = Math.max(0, req.getStartRow());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 1) лёгкий запрос только по id (ТИП Integer, а не Long)
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        // WHERE по filterModel
        List<Predicate> predicates =
                GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // ORDER BY
        if (req.getSortModel() != null && !req.getSortModel().isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.getSortModel().forEach(s -> {
                Path<?> p = GridTablePredicateBuilder.resolvePath(root, s.getColId());
                if ("desc".equalsIgnoreCase(s.getSort())) {
                    orders.add(cb.desc(p));
                } else {
                    orders.add(cb.asc(p));
                }
            });
            cq.orderBy(orders);
        } else {
            // дефолт: новые id сверху
            cq.orderBy(cb.desc(root.get("id")));
        }

        // выбираем только id (без .as(Long.class))
        cq.select(root.get("id"));

        List<Integer> ids = em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

        if (ids.isEmpty()) {
            return List.of();
        }

        // 2) тянем сущности по id
        List<Vehicle> items = em.createQuery(
                        "select v from Vehicle v where v.id in :ids",
                        Vehicle.class)
                .setParameter("ids", ids)
                .getResultList();

        // восстановить порядок как в ids
        Map<Integer, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) {
            rank.put(ids.get(i), i);
        }

        items.sort(Comparator.comparingInt(
                v -> rank.getOrDefault(v.getId(), Integer.MAX_VALUE)
        ));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates =
                GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
