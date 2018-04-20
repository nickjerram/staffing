package org.camra.staffing.data.provider;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.camra.staffing.data.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class SessionDataProvider extends SortableDataProvider <SessionSelectorDTO, Session> {

    @Autowired private SessionService sessionService;

    private List<QuerySortOrder> defaultSorting = new ArrayList<>();

    @PostConstruct
    private void init() {
        defaultSorting.add(new QuerySortOrder("start", SortDirection.ASCENDING));
    }

    @Override
    protected Stream<SessionSelectorDTO> fetchFromBackEnd(Query<SessionSelectorDTO, String> query) {
        Sort sort = doSortQuery(query, defaultSorting);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return sessionService.getSessionsWithAmounts(buildSpecification(), pr);
    }

    @Override
    protected int sizeInBackEnd(Query<SessionSelectorDTO, String> query) {
        return sessionService.getSessionCount(buildSpecification());
    }
}
