package org.camra.staffing.data.provider;

import org.camra.staffing.data.specification.*;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class SortableDataProvider<DTO,E> extends AbstractBackEndDataProvider<DTO,String> {

    Map<String,Specification<E>> specificationMap = new HashMap<>();

    /**
     * Convert Vaadin Sort query into Spring JPA Sort, using the default sorting if no sorting is specified in the query
     * @param query
     * @return
     */
    protected Sort doSortQuery(Query<DTO, String> query, List<QuerySortOrder> defaultSorting) {
        Sort sort = null;
        List<QuerySortOrder> sortOrders = new ArrayList<>();
        if (query.getSortOrders().isEmpty()) {
            sortOrders.addAll(defaultSorting);
        } else {
            sortOrders.addAll(query.getSortOrders());
        }

        for (QuerySortOrder order : sortOrders) {
            Sort sortSpecification = createSortCriterion(order);
            if (sort==null) {
                sort = sortSpecification;
            } else {
                sort = sort.and(sortSpecification);
            }
        }
        return sort;
    }

    /**
     * Convert a Vaadin sort specification into a Spring JPA sort specification
     * @param order
     * @return
     */
    private Sort createSortCriterion(QuerySortOrder order) {
        String field = order.getSorted();
        field = field.contains(".") ? field.split("\\.")[1] : field;
        SortDirection direction = order.getDirection();
        boolean asc = direction==SortDirection.ASCENDING;
        return new Sort(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field);
    }

    public void addStringFilter(String columnId, String filter) {
        StringCriterion<E> criterion = new StringCriterion<>(columnId, filter);
        specificationMap.put(columnId, criterion);
        refreshAll();
    }

    public void addRatioFilter(String columnId, String topProperty, String bottomProperty, String ratioProperty, String expression) {
        RatioCriterion<E> criterion = new RatioCriterion<>(topProperty, bottomProperty, ratioProperty);
        criterion.setFilter(expression);
        specificationMap.put(columnId, criterion);
        refreshAll();
    }

    public void addBooleanFilter(String columnId, BooleanCriterion.State state) {
        BooleanCriterion<E> criterion = new BooleanCriterion<>(columnId, state);
        specificationMap.put(columnId, criterion);
        refreshAll();
    }

    public void addIntegerCriterion(String property, int value) {
        IdCriterion<E> criterion = new IdCriterion<>("id",property, value);
        specificationMap.put(property, criterion);
        refreshAll();
    }

    Specification<E> buildSpecification() {
        Specification<E> result = new True<>();
        for (Specification<E> specification : specificationMap.values()) {
            result = Specification.where(result).and(specification);
        }
        return result;
    }

}