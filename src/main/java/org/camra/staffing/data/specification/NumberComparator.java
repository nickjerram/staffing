package org.camra.staffing.data.specification;

import lombok.Getter;

@Getter
class NumberComparator {

    enum ComparisonType {X,GT,GE,EQ,LE,LT}
    private double value;
    private ComparisonType type;

    NumberComparator() {
        type = ComparisonType.X;
    }

    NumberComparator(String fromString) {
        if (fromString==null) type = ComparisonType.X;
        else if (fromString.startsWith(">=")) type = ComparisonType.GE;
        else if (fromString.startsWith(">")) type = ComparisonType.GT;
        else if (fromString.startsWith("<=")) type = ComparisonType.LE;
        else if (fromString.startsWith("<")) type = ComparisonType.LT;
        else type = ComparisonType.EQ;
        double percentageFactor = fromString.endsWith("%") ? 0.01 : 1;
        try {
            fromString = fromString.replaceAll("[^0-9]+", "");
            value = Double.parseDouble(fromString)*percentageFactor;
        } catch (Exception e) {
            type = ComparisonType.X;
        }
    }

}
