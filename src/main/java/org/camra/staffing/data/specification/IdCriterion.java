package org.camra.staffing.data.specification;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class IdCriterion<E> implements Specification<E> {

    private String embeddable;
    private String inner;
    private int filter;

    public IdCriterion(String embeddable, String inner, int filter) {
        this.embeddable = embeddable;
        this.inner = inner;
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return builder.equal(root.get(embeddable).get(inner), filter);
    }
}
