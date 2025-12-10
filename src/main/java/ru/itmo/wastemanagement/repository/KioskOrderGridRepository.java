package ru.itmo.wastemanagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.KioskOrder;
import ru.itmo.wastemanagement.util.GridTablePredicateBuilder;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class KioskOrderGridRepository {

    @PersistenceContext
    private final EntityManager em;

    public List<KioskOrder> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.getEndRow() - req.getStartRow());
        final int offset = Math.max(0, req.getStartRow());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 1) лёгкий запрос только по id
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<KioskOrder> root = cq.from(KioskOrder.class);

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
            // дефолт — новые сверху
            cq.orderBy(
                    cb.desc(root.get("createdAt")),
                    cb.desc(root.get("id"))
            );
        }

        // выбираем только id
        cq.select(root.get("id").as(Long.class));

        List<Long> ids = em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

        if (ids.isEmpty()) {
            return List.of();
        }

        // 2) тянем сущности по id с fetch join'ами, чтобы не ловить N+1
        List<KioskOrder> items = em.createQuery(
                        "select ko from KioskOrder ko " +
                                "left join fetch ko.garbagePoint gp " +
                                "left join fetch ko.containerSize cs " +
                                "left join fetch ko.fraction fr " +
                                "left join fetch ko.user u " +
                                "where ko.id in :ids",
                        KioskOrder.class
                )
                .setParameter("ids", ids)
                .getResultList();

        // восстановить порядок как в ids
        Map<Long, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) {
            rank.put(ids.get(i), i);
        }

        items.sort(Comparator.comparingInt(
                ko -> rank.getOrDefault(ko.getId().longValue(), Integer.MAX_VALUE)
        ));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<KioskOrder> root = cq.from(KioskOrder.class);

        List<Predicate> predicates =
                GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
