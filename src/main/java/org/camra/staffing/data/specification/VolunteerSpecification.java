package org.camra.staffing.data.specification;

import org.camra.staffing.data.entity.Volunteer;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


public class VolunteerSpecification implements Specification<Volunteer> {

    private SearchCriterion criterion;

    public VolunteerSpecification(SearchCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public Predicate toPredicate(Root<Volunteer> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criterion!=null && criterion.getOperation().equals(":")) {
            if (root.get(criterion.getKey()).getJavaType() == String.class) {
                return builder.like(builder.upper(root.get(criterion.getKey())), "%" + criterion.getValue().toUpperCase() + "%");
            } else {
                return builder.equal(root.get(criterion.getKey()), criterion.getValue());
            }
        } else {
            return builder.and();
        }
    }

}
