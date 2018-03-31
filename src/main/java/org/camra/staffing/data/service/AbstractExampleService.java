package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExampleService<DTO,ENTITY> {

    public abstract List<DTO> getRecords(Query<DTO, Example<ENTITY>> query);

    public abstract int countRecords(Query<DTO,Example<ENTITY>> query);

    /**
     * Convert the Sorting and Paging components of a Vaadin Query into a Spring JPA PageRequest
     */
    protected Pageable pageRequest(Query<?, ?> query, String defaultSort) {
        List<Sort.Order> springSorts = new ArrayList<>();
        for (QuerySortOrder sortOrder : query.getSortOrders()) {
            springSorts.add(new Sort.Order(direction(sortOrder.getDirection()), sortOrder.getSorted()));
        }
        if (springSorts.isEmpty()) {
            springSorts.add(new Sort.Order(Sort.Direction.ASC, defaultSort));
        }
        Sort finalSort = new Sort(springSorts);

        //int pageNumber = query.getOffset() / query.getLimit();
        //System.out.println(" query offset:"+query.getOffset()+" limit="+query.getLimit()+" --> page:"+pageNumber+" count:"+query.getLimit());
        Pair<Integer,Integer> springPage = limitAndOffsetToPageSizeAndNumber(query.getOffset(), query.getLimit());
        int pageSize = springPage.getFirst();
        int pageNumber = springPage.getSecond();
        System.out.println("Offset "+query.getOffset()+" limit "+query.getLimit()+" --> page "+pageNumber+" size "+pageSize);
        return new PageRequest(pageNumber, pageSize, finalSort);
        //return new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), finalSort);
    }

    private Sort.Direction direction(SortDirection sortDirection) {
        return sortDirection==SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    public static Pair<Integer, Integer> limitAndOffsetToPageSizeAndNumber(
            int offset, int limit) {
        int minPageSize = limit;
        int lastIndex = offset + limit - 1;
        int maxPageSize = lastIndex + 1;

        for (double pageSize = minPageSize; pageSize <= maxPageSize; pageSize++) {
            int startPage = (int) (offset / pageSize);
            int endPage = (int) (lastIndex / pageSize);
            if (startPage == endPage) {
                // It fits on one page, let's go with that
                return Pair.of((int) pageSize, startPage);
            }
        }

        // Should not really get here
        return Pair.of(maxPageSize, 0);
    }

}
