package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.AreaSelector;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.repository.AreaSelectorRepository;
import org.camra.staffing.data.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService {

    @Autowired private VolunteerRepository volunteerRepository;
    @Autowired private AreaSelectorRepository areaSelectorRepository;

    public List<VolunteerDTO> getVolunteers(Query<VolunteerDTO,Example<Volunteer>> query) {
        if (query.getFilter().isPresent()) {
            return toDTOs(volunteerRepository.findAll(query.getFilter().get(), pageRequest(query)));
        } else {
            return toDTOs(volunteerRepository.findAll(pageRequest(query)));
        }
    }

    public int countVolunteers(Query<VolunteerDTO,Example<Volunteer>> query) {
        if (query.getFilter().isPresent()) {
            return (int) volunteerRepository.count(query.getFilter().get());
        } else {
            return (int) volunteerRepository.count();
        }
    }

    public List<AreaSelectorDTO> getAreaSelectors(Integer volunteerId) {
        List<AreaSelector> selectors = areaSelectorRepository.findByIdVolunteerId(volunteerId);
        List<AreaSelectorDTO> dtos = new ArrayList<>();
        selectors.forEach(selector -> dtos.add(AreaSelectorDTO.fromAreaSelector(selector)));
        return dtos;
    }

    private List<VolunteerDTO> toDTOs(Iterable<Volunteer> volunteers) {
        List<VolunteerDTO> result = new ArrayList<>();
        volunteers.forEach(volunteer -> result.add(new VolunteerDTO(volunteer)));
        return result;
    }

    /**
     * Convert the Sorting and Paging components of a Vaadin Query into a Spring JPA PageRequest
     * @param query
     * @return Page Request
     */
    private Pageable pageRequest(Query<?, ?> query) {
        List<Order> springSorts = new ArrayList<>();
        for (QuerySortOrder sortOrder : query.getSortOrders()) {
            springSorts.add(new Order(direction(sortOrder.getDirection()), sortOrder.getSorted()));
        }
        if (springSorts.isEmpty()) {
            springSorts.add(new Order(Direction.ASC, "surname"));
        }
        Sort finalSort = new Sort(springSorts);

        int pageNumber = query.getOffset() / query.getLimit();
        return new PageRequest(pageNumber, query.getLimit(), finalSort);
    }

    private Direction direction(SortDirection sortDirection) {
        return sortDirection==SortDirection.ASCENDING ? Direction.ASC : Direction.DESC;
    }

}
