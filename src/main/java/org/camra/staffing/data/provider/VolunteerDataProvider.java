package org.camra.staffing.data.provider;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.camra.staffing.data.dto.BadgeDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.service.MainViewService;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.Query;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class VolunteerDataProvider extends SortableDataProvider<VolunteerDTO,Volunteer>{

    @Autowired private VolunteerService volunteerService;

    private List<QuerySortOrder> defaultSorting = new ArrayList<>();

    public List<BadgeDTO> getBadges() {
        return volunteerService.getBadges(buildSpecification()).collect(Collectors.toList());
    }

    @PostConstruct
    private void init() {
        defaultSorting.add(new QuerySortOrder("surname", SortDirection.ASCENDING));
        defaultSorting.add(new QuerySortOrder("forename", SortDirection.ASCENDING));
    }

    @Override
    protected Stream<VolunteerDTO> fetchFromBackEnd(Query<VolunteerDTO, String> query) {
        Sort sort = doSortQuery(query, defaultSorting);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return volunteerService.getVolunteers(buildSpecification(), pr);
    }

    @Override
    protected int sizeInBackEnd(Query<VolunteerDTO, String> query) {
        return volunteerService.getVolunteerCount(buildSpecification());
    }

}