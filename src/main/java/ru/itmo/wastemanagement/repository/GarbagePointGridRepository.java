package ru.itmo.wastemanagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.GarbagePoint;
import ru.itmo.wastemanagement.util.GridTablePredicateBuilder;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class GarbagePointGridRepository {

    @PersistenceContext
    private final EntityManager em;

    public List<GarbagePoint> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.getEndRow() - req.getStartRow());
        final int offset = Math.max(0, req.getStartRow());

        var cb = em.getCriteriaBuilder();

        // 1) лёгкий запрос только по id
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<GarbagePoint> root = cq.from(GarbagePoint.class);

        // WHERE по filterModel
        var predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
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
            // дефолт: новые точки сверху
            cq.orderBy(
                    cb.desc(root.get("createdAt")),
                    cb.desc(root.get("id"))
            );
        }

        // ВАЖНО: id как Integer
        cq.select(root.get("id").as(Integer.class));

        List<Integer> ids = em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

        if (ids.isEmpty()) {
            return List.of();
        }

        // 2) тянем сущности по id (можно добавить fetch join'ы, если надо)
        List<GarbagePoint> items = em.createQuery(
                        "select gp from GarbagePoint gp " +
                                "left join fetch gp.admin " +
                                "left join fetch gp.kiosk " +
                                "where gp.id in :ids", GarbagePoint.class)
                .setParameter("ids", ids)
                .getResultList();

        // восстановить порядок
        Map<Integer, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) {
            rank.put(ids.get(i), i);
        }

        items.sort(Comparator.comparingInt(
                gp -> rank.getOrDefault(gp.getId(), Integer.MAX_VALUE)
        ));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        var cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<GarbagePoint> root = cq.from(GarbagePoint.class);

        var predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
