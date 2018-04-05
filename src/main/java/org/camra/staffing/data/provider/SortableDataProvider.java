package org.camra.staffing.data.provider;

import org.camra.staffing.data.specification.SearchCriterion;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public abstract class SortableDataProvider<DTO,E> extends AbstractBackEndDataProvider<DTO,String> {

    private Map<String, SearchCriterion> criteria = new HashMap<>();

    protected Specification<E> specification;

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

    public void addFilter(String columnId, String filter) {
        criteria.put(columnId, new SearchCriterion(columnId,":", filter));
        List<Specification<E>> specificationList = new ArrayList<>();
        for (SearchCriterion criterion : criteria.values()) {
            specificationList.add(createSpecification(criterion));
        }
        specification = specificationList.get(0);
        for (int i=1; i<specificationList.size(); i++) {
            specification = Specifications.where(specification).and(specificationList.get(i));
        }
        refreshAll();
    }

    protected abstract Specification<E> createSpecification(SearchCriterion criterion);


}