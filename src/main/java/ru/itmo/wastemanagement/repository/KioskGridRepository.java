package ru.itmo.wastemanagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.User;
import ru.itmo.wastemanagement.entity.enums.UserRole;
import ru.itmo.wastemanagement.util.GridTablePredicateBuilder;

import java.util.*;

@Repository
public class KioskGridRepository {

    @PersistenceContext
    private EntityManager em;

    public List<User> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.getEndRow() - req.getStartRow());
        final int offset = Math.max(0, req.getStartRow());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 1) лёгкий запрос только по id
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<User> root = cq.from(User.class);

        // WHERE из filterModel
        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        // всегда только KIOSK
        predicates.add(cb.equal(root.get("role"), UserRole.KIOSK));

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

        // 2) тянем сущности по id
        List<User> items = em.createQuery(
                        "select u from User u where u.id in :ids and u.role = :role", User.class)
                .setParameter("ids", ids)
                .setParameter("role", UserRole.KIOSK)
                .getResultList();

        // восстановить порядок как в ids
        Map<Integer, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) {
            rank.put(ids.get(i), i);
        }

        items.sort(Comparator.comparingInt(
                u -> rank.getOrDefault(u.getId(), Integer.MAX_VALUE)
        ));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        predicates.add(cb.equal(root.get("role"), UserRole.KIOSK));

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
