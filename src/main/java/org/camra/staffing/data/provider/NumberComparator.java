package org.camra.staffing.data.provider;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class NumberComparator {

    private enum ComparisonType {X,GT,GE,EQ,LE,LT}
    private int value;
    private ComparisonType type;

    NumberComparator(String fromString) {
        if (fromString==null) type = ComparisonType.X;
        else if (fromString.startsWith(">=")) type = ComparisonType.GE;
        else if (fromString.startsWith(">")) type = ComparisonType.GT;
        else if (fromString.startsWith("<=")) type = ComparisonType.LE;
        else if (fromString.startsWith("<")) type = ComparisonType.LT;
        else type = ComparisonType.EQ;
        try {
            fromString = fromString.replaceAll("[^0-9]+", "");
            value = Integer.parseInt(fromString);
        } catch (Exception e) {
            type = ComparisonType.X;
        }
    }

    Predicate toPredicate(CriteriaBuilder cb, Root<?> root, String field) {
        switch (type) {
            case GE: return cb.ge(root.get(field), value);
            case GT: return cb.gt(root.get(field), value);
            case LE: return cb.le(root.get(field), value);
            case LT: return cb.lt(root.get(field), value);
            case EQ: return cb.equal(root.get(field), value);
            default: return cb.and();
        }
    }
}
