package org.camra.staffing.data.specification;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public class SearchCriterion {

    public SearchCriterion(String key, String operation, String value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    private String key;
    private String operation;
    private String value;
}
