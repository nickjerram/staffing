package org.camra.staffing.data.provider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


class RatioFilter {

    private String topField;
    private String bottomField;
    private String ratioField;
    private NumberComparator topExpression = new NumberComparator(null);
    private NumberComparator bottomExpression = new NumberComparator(null);
    private NumberComparator ratioExpression = new NumberComparator(null);

    RatioFilter(String topField, String bottomField, String ratioField, String expression) {
        this.topField = topField;
        this.bottomField = bottomField;
        this.ratioField = ratioField;
        if (expression.contains("/")) {
            String[] parts = expression.split("/");
            topExpression = new NumberComparator(parts.length>0 ? parts[0] : null);
            bottomExpression = new NumberComparator(parts.length>1 ? parts[1] : null);
        } else if (expression.endsWith("%")) {
            ratioExpression = new NumberComparator(expression.replace("%",""));
        } else{
            topExpression = new NumberComparator(expression);
        }

    }

    Predicate topPredicate(CriteriaBuilder cb, Root<?> root) {
        return topExpression.toPredicate(cb, root, topField);
    }

    Predicate bottomPredicate(CriteriaBuilder cb, Root<?> root) {
        return bottomExpression.toPredicate(cb, root, bottomField);
    }

    Predicate ratioPredicate(CriteriaBuilder cb, Root<?> root) {
        return ratioExpression.toPredicate(cb, root, ratioField);
    }

}
