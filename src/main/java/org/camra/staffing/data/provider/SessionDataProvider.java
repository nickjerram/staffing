package org.camra.staffing.data.provider;

import com.vaadin.data.provider.Query;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.camra.staffing.data.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.stream.Stream;

@SpringComponent
@UIScope
public class SessionDataProvider extends SortableDataProvider <SessionDTO, Session> {

    @Autowired private SessionService sessionService;

    @Override
    protected Stream<SessionDTO> fetchFromBackEnd(Query<SessionDTO, String> query) {
        Sort sort = doSortQuery(query);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return sessionService.getSessions(buildSpecification(), pr);
    }

    @Override
    protected int sizeInBackEnd(Query<SessionDTO, String> query) {
        return sessionService.getSessionCount(buildSpecification());
    }
}
