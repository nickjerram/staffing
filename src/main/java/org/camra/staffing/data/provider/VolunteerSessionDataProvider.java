package org.camra.staffing.data.provider;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.camra.staffing.data.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringComponent
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VolunteerSessionDataProvider extends SortableDataProvider<VolunteerSessionDTO,VolunteerSessionView> {

    @Autowired private SessionService sessionService;

    public void setSessionId(int sessionId) {
        addIntegerCriterion("sessionId", sessionId);
    }

    public void setVolunteerId(int volunteerId) {
        addIntegerCriterion("volunteerId", volunteerId);
    }

    private List<QuerySortOrder> defaultSorting = new ArrayList<>();

    @PostConstruct
    private void init() {
        defaultSorting.add(new QuerySortOrder("volunteer.surname", SortDirection.ASCENDING));
        defaultSorting.add(new QuerySortOrder("volunteer.forename", SortDirection.ASCENDING));
    }

    @Override
    protected Stream<VolunteerSessionDTO> fetchFromBackEnd(Query<VolunteerSessionDTO, String> query) {
        Sort sort = doSortQuery(query, defaultSorting);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return sessionService.getAssignments(buildSpecification(), pr);
    }

    @Override
    protected int sizeInBackEnd(Query<VolunteerSessionDTO, String> query) {
        return sessionService.getAssignmentCount(buildSpecification());
    }

}
