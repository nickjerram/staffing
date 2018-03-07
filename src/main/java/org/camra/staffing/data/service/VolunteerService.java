package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.AssignmentSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.*;
import org.camra.staffing.data.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class VolunteerService {

    @Autowired private VolunteerRepository volunteerRepository;
    @Autowired private AreaSelectorRepository areaSelectorRepository;
    @Autowired private AssignableAreaRepository assignableAreaRepository;
    @Autowired private AssignedCountRepository assignedCountRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private VolunteerSessionRepository volunteerSessionRepository;

    public List<VolunteerDTO> getVolunteers(Query<VolunteerDTO,Example<Volunteer>> query) {
        if (query.getFilter().isPresent()) {
            return toDTOs(volunteerRepository.findAll(query.getFilter().get(), pageRequest(query, "surname")));
        } else {
            return toDTOs(volunteerRepository.findAll(pageRequest(query, "surname")));
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

    public void saveVolunteer(VolunteerDTO volunteerDTO) {
        Volunteer toSave = volunteerDTO.getId()==0 ? new Volunteer() : volunteerRepository.findOne(volunteerDTO.getId());
        volunteerDTO.populateVolunteer(toSave);
        Volunteer saved = volunteerRepository.save(toSave);
        VolunteerAreaAssigner assigner = new VolunteerAreaAssigner(saved);
        assigner.assign(volunteerDTO.getAreas().values());
        volunteerRepository.save(saved);
    }

    public List<VolunteerSessionDTO> getSessions(int volunteerId, Query<VolunteerSessionDTO,Void> query) {
        List<VolunteerSession> vss = volunteerSessionRepository.findByIdVolunteerId(volunteerId, pageRequest(query, "SessionStart"));
        List<VolunteerSessionDTO> result = new ArrayList<>();
        vss.forEach(volunteerSession -> {
            VolunteerSessionDTO dto = VolunteerSessionDTO.create(volunteerSession);
            AssignedCounts counts = assignedCountRepository.findByIdAreaIdAndIdSessionId(volunteerSession.getArea().getId(), volunteerSession.getSession().getId());
            dto.setStaffAssigned(counts.getAssigned());
            dto.setStaffRequired(counts.getRequired());
            dto.setTokensDue(0);
            result.add(dto);
        });
        return result;
    }

    public int countSessions(int volunteerId) {
        return (int) volunteerSessionRepository.countByIdVolunteerId(volunteerId);
    }

    public List<AssignmentSelectorDTO> getPossibleReassignments(VolunteerSessionDTO volunteerSession) {
        Volunteer volunteer = volunteerRepository.findOne(volunteerSession.getVolunteerId());
        List<VolunteerArea> areas = volunteer.getAreas();
        List<AssignmentSelectorDTO> result = new ArrayList<>();
        for (VolunteerArea volunteerArea: areas) {
            AssignedCounts counts = assignedCountRepository.findByIdAreaIdAndIdSessionId(volunteerArea.getAssignableArea().getId(), volunteerSession.getSessionId());
            AssignmentSelectorDTO dto = AssignmentSelectorDTO.populateFields(volunteerArea);
            dto.setCurrent(volunteerSession.getAreaId()==volunteerArea.getAssignableArea().getId());
            dto.setStaffAssigned(counts.getAssigned());
            dto.setStaffRequired(counts.getRequired());
            result.add(dto);
        }
        return result;
    }

    public void saveAssignment(VolunteerSessionDTO volunteerSession) {
        Volunteer v = volunteerRepository.findOne(volunteerSession.getVolunteerId());
        Session session = sessionRepository.findOne(volunteerSession.getSessionId());
        AssignableArea area = assignableAreaRepository.findOne(volunteerSession.getAreaId());
        VolunteerSession vs = v.getSessionMap().get(volunteerSession.getSessionId());
        vs.setLocked(volunteerSession.isLock());
        vs.setWorked(volunteerSession.isWorked());
        vs.setTokens(volunteerSession.getTokens());
        vs.setComment(volunteerSession.getComment());
        v.reassign(area, session);
        volunteerRepository.saveAndFlush(v);
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
    private Pageable pageRequest(Query<?, ?> query, String defaultSort) {
        List<Order> springSorts = new ArrayList<>();
        for (QuerySortOrder sortOrder : query.getSortOrders()) {
            springSorts.add(new Order(direction(sortOrder.getDirection()), sortOrder.getSorted()));
        }
        if (springSorts.isEmpty()) {
            springSorts.add(new Order(Direction.ASC, defaultSort));
        }
        Sort finalSort = new Sort(springSorts);

        int pageNumber = query.getOffset() / query.getLimit();
        return
                new PageRequest(pageNumber, query.getLimit(), finalSort);
    }

    private Direction direction(SortDirection sortDirection) {
        return sortDirection==SortDirection.ASCENDING ? Direction.ASC : Direction.DESC;
    }

    private class VolunteerAreaAssigner {

        private Volunteer volunteer;
        private AssignableArea unassigned = assignableAreaRepository.getUnassigned();

        private VolunteerAreaAssigner(Volunteer volunteer) {
            this.volunteer = volunteer;
        }

        public void assign(Collection<AreaSelectorDTO> areas) {
            areas.forEach(this::assignToFormArea);
        }

        private void assignToFormArea(AreaSelectorDTO areaSelectorDTO) {
            assignableAreaRepository.findByFormAreaId(areaSelectorDTO.getAreaId()).forEach(area -> {
                if (areaSelectorDTO.getPreference()==Preference.No) {
                    volunteer.removeArea(area, unassigned);
                } else {
                    volunteer.addArea(area, areaSelectorDTO.getPreference());
                }
            });
        }

    }
}
