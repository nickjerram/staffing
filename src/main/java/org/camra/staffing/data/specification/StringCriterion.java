package org.camra.staffing.data.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringCriterion<E> implements Specification<E> {

    private String property;
    private List<String> filter;

    public StringCriterion(String property, String filter) {
        this.property = property;
        this.filter = Stream.of(filter.split("\\s+")).collect(Collectors.toList());
    }

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = filter.stream().map(exp-> partPredicate(exp, root, builder)).collect(Collectors.toList());
        return builder.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate partPredicate(String expression, Root<E> root, CriteriaBuilder builder) {
        return builder.like(builder.upper(root.get(property)), "%"+expression.toUpperCase()+"%");
    }
}
