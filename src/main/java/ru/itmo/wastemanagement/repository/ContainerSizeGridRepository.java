package ru.itmo.wastemanagement.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.itmo.wastemanagement.dto.gridtable.GridTableRequest;
import ru.itmo.wastemanagement.entity.ContainerSize;
import ru.itmo.wastemanagement.util.GridTablePredicateBuilder;

@Repository
@RequiredArgsConstructor
public class ContainerSizeGridRepository {

    @PersistenceContext
    private final EntityManager em;

    public List<ContainerSize> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.getEndRow() - req.getStartRow());
        final int offset = Math.max(0, req.getStartRow());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ContainerSize> root = cq.from(ContainerSize.class);

        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (req.getSortModel() != null && !req.getSortModel().isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.getSortModel().forEach(s -> {
                Path<?> p = GridTablePredicateBuilder.resolvePath(root, s.getColId());
                orders.add("desc".equalsIgnoreCase(s.getSort()) ? cb.desc(p) : cb.asc(p));
            });
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.desc(root.get("id")));
        }

        cq.select(root.get("id"));

        List<Long> ids = em.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

        if (ids.isEmpty()) {
            return List.of();
        }

        List<ContainerSize> items = em.createQuery(
                        "select c from ContainerSize c where c.id in :ids",
                        ContainerSize.class)
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) {
            rank.put(ids.get(i), i);
        }

        items.sort(Comparator.comparingInt(
                c -> rank.getOrDefault(c.getId(), Integer.MAX_VALUE)
        ));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ContainerSize> root = cq.from(ContainerSize.class);

        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, root, req.getFilterModel());
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}
