package org.camra.staffing.data.provider;

import org.camra.staffing.data.specification.RatioCriterion;
import org.camra.staffing.data.specification.StringCriterion;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.HashMap;
import java.util.Map;


public abstract class SortableDataProvider<DTO,E> extends AbstractBackEndDataProvider<DTO,String> {

    Map<String,Specification<E>> specificationMap = new HashMap<>();

    /**
     * Convert Vaadin Sort query into Spring JPA Sort
     * @param query
     * @return
     */
    protected Sort doSortQuery(Query<DTO, String> query) {
        Sort sort = null;

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

    Specification<E> buildSpecification() {
        Specification<E> result = Specifications.where(null);
        for (Specification<E> specification : specificationMap.values()) {
            result = Specifications.where(result).and(specification);
        }
        return result;
    }

}