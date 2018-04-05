package org.camra.staffing.data.specification;

import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Getter
public class RatioCriterion<E> implements Specification<E> {

    private String topProperty;
    private String bottomProperty;
    private String ratioProperty;

    private NumberComparator topFilter = new NumberComparator();
    private NumberComparator bottomFilter = new NumberComparator();
    private NumberComparator percentFilter = new NumberComparator();

    public RatioCriterion(String topProperty, String bottomProperty, String ratioProperty) {
        this.topProperty = topProperty;
        this.bottomProperty = bottomProperty;
        this.ratioProperty = ratioProperty;
    }

    public void setFilter(String filter) {
        if (StringUtils.hasText(filter)) {
            if (filter.contains("/")) {
                String[] parts = filter.split("/");
                topFilter = new NumberComparator(parts.length>0 ? parts[0] : null);
                bottomFilter = new NumberComparator(parts.length>1 ? parts[1] : null);
            } else if (filter.endsWith("%")) {
                percentFilter = new NumberComparator(filter);
            } else {
                topFilter = new NumberComparator(filter);
            }
        }
    }

    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Predicate topPredicate = partPredicate(topFilter, topProperty, root, builder);
        Predicate bottomPredicate = partPredicate(bottomFilter, bottomProperty, root, builder);
        Predicate percentPredicate = partPredicate(percentFilter, ratioProperty, root, builder);

        return builder.and(topPredicate, bottomPredicate, percentPredicate);
    }

    private Predicate partPredicate(NumberComparator comparator, String key, Root<E> root, CriteriaBuilder builder) {
        switch (comparator.getType()) {
            case EQ: return builder.equal(root.get(key), comparator.getValue());
            case LT: return builder.lessThan(root.get(key), comparator.getValue());
            case LE: return builder.lessThanOrEqualTo(root.get(key), comparator.getValue());
            case GE: return builder.greaterThanOrEqualTo(root.get(key), comparator.getValue());
            case GT: return builder.greaterThan(root.get(key), comparator.getValue());
            default: return builder.and();
        }
    }


}
