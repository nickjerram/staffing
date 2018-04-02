package org.camra.staffing.data.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;

public abstract class SortableDataProvider<DTO,E> extends AbstractBackEndDataProvider<DTO,String> {

    private Map<String,String> stringFilterMap = new HashMap<>();
    private Map<String,List<String>> multiStringFilterMap = new HashMap<>();
    private Map<String,RatioFilter> ratioFilterMap = new HashMap<>();
    private Map<String,Boolean> booleanFilterMap = new HashMap<>();

    /**
     * Convert Vaadin Sort query into Spring JPA Sort
     * @param query
     * @return
     */
    protected Sort doSortQuery(Query<DTO, String> query) {
        Sort sort = null;

        //Assemble Sort
        //Sorting takes place at the database layer
        for (QuerySortOrder order : query.getSortOrders()) {
            String field = order.getSorted();
            field = field.contains(".") ? field.split("\\.")[1] : field;
            SortDirection direction = order.getDirection();
            boolean asc = direction==SortDirection.ASCENDING;
            if (sort==null) {
                sort = new Sort(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field);
            } else {
                sort = sort.and(new Sort(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field));
            }
        }
        return sort;
    }

    /**
     * Add a simple String Filter for a column consisting of only one field
     * Called when the user enters text into a Column Filter field
     * @param field
     * @param value
     */
    public void addStringFilter(String field, String value) {
        if (StringUtils.hasLength(value)) {
            stringFilterMap.put(field, value);
        } else {
            stringFilterMap.remove(field);
        }
    }

    /**
     * Add a multiple String Filter for a column consisting of multiple fields
     * Expressions are separated by whitespace and OR'd together to filter the appropriate fields
     * @param fields
     * @param values
     */
    public void addStringFilters(String[] fields, String[] values) {
        for (String field : fields) {
            if (values.length==0) {
                multiStringFilterMap.remove(field);
            } else {
                List<String> valueList = new ArrayList<>();
                multiStringFilterMap.put(field, valueList);
                for (String value : values) {
                    if (StringUtils.hasLength(value)) valueList.add(value);
                }
                if (valueList.isEmpty()) multiStringFilterMap.remove(field);
            }
        }
    }

    /**
     * Add Ratio Filter (for Ratio fields)
     * @param topField
     * @param bottomField
     * @param ratioField
     * @param expression
     */
    public void addRatioFilter(String topField, String bottomField, String ratioField, String expression) {
        String key = topField+bottomField+ratioField;
        if (StringUtils.hasLength(expression)) {
            ratioFilterMap.put(key, new RatioFilter(topField, bottomField, ratioField, expression));
        } else {
            ratioFilterMap.remove(key);
        }
    }

    /**
     * Add Boolean Filter (for boolean fields)
     * @param field
     * @param value
     */
    public void addBooleanFilter(String field, String value) {
        if (value==null) {
            booleanFilterMap.remove(field);
        } else {
            booleanFilterMap.put(field, value.equalsIgnoreCase("true"));
        }
    }

    /**
     * Construct and return Specification for the Spring JPA query
     * from the columns header filters
     * @return Specification
     */
    protected Specification<E> matches() {
        return new Specification<E>() {

            public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

                List<Predicate> predicates = doFilterPredicates(root, cb);
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }

    /**
     * Build list of Predicates to be applied to the Query depending on Filters
     * @param root
     * @param cb
     * @return
     */
    private List<Predicate> doFilterPredicates(Root<E> root, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<Predicate>();

        //add string filters
        for (String field : stringFilterMap.keySet()) {
            String valuePattern = stringFilterMap.get(field);
            String[] parts = valuePattern.split("\\s");
            for (String part : parts) {
                predicates.add(cb.like(root.get(field), "%"+part+"%"));
            }
        }

        //add multi-string filters
        //create a map of Predicates
        //keys are field values, lists are all predicates for the value (to be ORd together)
        Map<String,List<Predicate>> predicateMatrix = new HashMap<>();
        for (String field : multiStringFilterMap.keySet()) {
            List<String> values = multiStringFilterMap.get(field);
            for (String value : values) {
                List<Predicate> valuePredicates = predicateMatrix.get(value);
                if (valuePredicates==null) {
                    valuePredicates = new ArrayList<>();
                    predicateMatrix.put(value, valuePredicates);
                }
                Predicate valuePredicate = cb.like(root.get(field), "%"+value+"%");
                valuePredicates.add(valuePredicate);
            }
        }

        //construct predicate
        List<Predicate> ors = new ArrayList<>();
        for (List<Predicate> valuePredicateList : predicateMatrix.values()) {
            ors.add(cb.or(valuePredicateList.toArray(new Predicate[valuePredicateList.size()])));
        }
        predicates.add(cb.and(ors.toArray(new Predicate[ors.size()])));

        //add ratio filters
        for (RatioFilter filter : ratioFilterMap.values()) {
            predicates.add(filter.topPredicate(cb, root));
            predicates.add(filter.bottomPredicate(cb, root));
            predicates.add(filter.ratioPredicate(cb, root));
        }

        //add boolean filters
        for (String field : booleanFilterMap.keySet()) {
            int value = booleanFilterMap.get(field) ? 1 : 0;
            predicates.add(cb.equal(root.get(field), value));
        }

        return predicates;

    }

}