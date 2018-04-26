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
    private List<String> and;
    private List<String> or;
    private String exact;

    public StringCriterion(String property, String filter) {
        this.property = property;
        if (filter.startsWith("=")) {
            this.exact = filter.length()>1 ? filter.substring(1) : "";
        } else if (filter.contains("|")) {
            this.or = Stream.of(filter.split("\\|")).collect(Collectors.toList());
        } else {
            this.and = Stream.of(filter.split("\\s+")).collect(Collectors.toList());
        }
    }

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (and!=null) {
            return andPredicate(root, builder);
        } else if (or!=null) {
            return orPredicate(root, builder);
        } else if (exact!=null) {
            return exactPredicate(root, builder);
        } else {
            return builder.and();
        }
    }

    private Predicate andPredicate(Root<E> root, CriteriaBuilder builder) {
        List<Predicate> predicates = and.stream().map(exp-> partPredicate(exp, root, builder)).collect(Collectors.toList());
        return builder.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate orPredicate(Root<E> root, CriteriaBuilder builder) {
        List<Predicate> predicates = or.stream().map(exp-> partPredicate(exp, root, builder)).collect(Collectors.toList());
        return builder.or(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate partPredicate(String expression, Root<E> root, CriteriaBuilder builder) {
        return builder.like(builder.upper(root.get(property)), "%"+expression.toUpperCase()+"%");
    }

    private Predicate exactPredicate(Root<E> root, CriteriaBuilder builder) {
        return builder.equal(builder.upper(root.get(property)), exact.toUpperCase());
    }

}
