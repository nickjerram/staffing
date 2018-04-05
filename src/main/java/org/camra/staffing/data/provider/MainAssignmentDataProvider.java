package org.camra.staffing.data.provider;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.entityviews.MainView;
import org.camra.staffing.data.service.MainViewService;
import org.camra.staffing.data.service.OffsetBasedPageRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vaadin.data.provider.Query;
import org.springframework.data.jpa.domain.Specification;

@SpringComponent
@UIScope
public class MainAssignmentDataProvider extends SortableDataProvider<MainViewDTO, MainView> {

    @Autowired private MainViewService service;

    public boolean isInMemory() {
        return false;
    }

    protected Stream<MainViewDTO> fetchFromBackEnd(Query<MainViewDTO, String> query) {
        Sort sort = doSortQuery(query);
        Pageable pr = new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
        return service.getRecords(buildSpecification(), pr);
    }

    protected int sizeInBackEnd(Query<MainViewDTO, String> query) {
        return service.getRecordCount(buildSpecification());
    }


}