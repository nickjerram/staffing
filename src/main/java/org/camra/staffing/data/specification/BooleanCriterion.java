package org.camra.staffing.data.specification;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class BooleanCriterion<E> implements Specification<E> {

    public enum State {YES,NO,X}

    private State state;
    private String column;

    public BooleanCriterion(String column, State state) {
        this.column = column;
        this.state = state;
    }

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (state==State.X) {
            return builder.and();
        } else {
            boolean value = state==State.YES;
            return builder.equal(root.get(column), value);
        }
    }
}
