package org.camra.staffing.data.provider;

import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.data.specification.SearchCriterion;
import org.camra.staffing.data.specification.VolunteerSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.Query;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.springframework.data.jpa.domain.Specification;

import java.util.stream.Stream;

@SpringComponent
@UIScope
public class VolunteerDataProvider extends SortableDataProvider<VolunteerDTO,Volunteer>{

    @Autowired private VolunteerService volunteerService;

    @Override
    protected Stream<VolunteerDTO> fetchFromBackEnd(Query<VolunteerDTO, String> query) {
        Sort sort = doSortQuery(query);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return volunteerService.getVolunteers(specification, pr);
    }

    @Override
    protected int sizeInBackEnd(Query<VolunteerDTO, String> query) {
        return volunteerService.getVolunteerCount(specification);
    }

    @Override
    protected Specification<Volunteer> createSpecification(SearchCriterion criterion) {
        return new VolunteerSpecification(criterion);
    }
}