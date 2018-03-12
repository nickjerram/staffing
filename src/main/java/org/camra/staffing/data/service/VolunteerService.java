package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import org.camra.staffing.data.dto.*;
import org.camra.staffing.data.entity.*;
import org.camra.staffing.data.entityviews.AssignmentSelectorView;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.camra.staffing.data.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VolunteerService {

    @Autowired private VolunteerRepository volunteerRepository;
    @Autowired private AreaSelectorRepository areaSelectorRepository;
    @Autowired private AssignableAreaRepository assignableAreaRepository;
    @Autowired private AssignmentSelectorViewRepository assignmentSelectorViewRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private VolunteerSessionRepository volunteerSessionRepository;
    @Autowired private PossibleSessionRepository possibleSessionRepository;

    public List<VolunteerDTO> getVolunteers(Query<VolunteerDTO,Example<Volunteer>> query) {
        Page<Volunteer> queryResult;
        if (query.getFilter().isPresent()) {
            queryResult = volunteerRepository.findAll(query.getFilter().get(), pageRequest(query, "surname"));
        } else {
            queryResult = volunteerRepository.findAll(pageRequest(query, "surname"));
        }
        return queryResult.getContent().stream().map(VolunteerDTO::create).collect(Collectors.toList());
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
        return selectors.stream().map(AreaSelectorDTO::create).collect(Collectors.toList());
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
        List<VolunteerSessionView> volunteerSessions = volunteerSessionRepository.findByIdVolunteerId(volunteerId, pageRequest(query, "sessionStart"));
        return volunteerSessions.stream().map(VolunteerSessionDTO::create).collect(Collectors.toList());
    }


    public int countSessions(int volunteerId) {
        return (int) volunteerSessionRepository.countByIdVolunteerId(volunteerId);
    }

    public List<AssignmentSelectorDTO> getPossibleReassignments(VolunteerSessionDTO volunteerSession) {
        List<AssignmentSelectorView> selectorViews =
                assignmentSelectorViewRepository.findByIdVolunteerIdAndIdSessionId(volunteerSession.getVolunteerId(), volunteerSession.getSessionId());
        return selectorViews.stream().map(AssignmentSelectorDTO::create).collect(Collectors.toList());
    }

    public List<SessionSelectorDTO> getPossibleSessions(int volunteerId) {
        List<PossibleSession> possibleSessions = possibleSessionRepository.findByIdVolunteerId(volunteerId);
        return possibleSessions.stream().map(SessionSelectorDTO::create).collect(Collectors.toList());
    }

    public void saveAssignment(VolunteerSessionDTO volunteerSession) {
        Volunteer v = volunteerRepository.findOne(volunteerSession.getVolunteerId());
        Session session = sessionRepository.findOne(volunteerSession.getSessionId());
        AssignableArea area = assignableAreaRepository.findOne(volunteerSession.getAreaId());
        VolunteerSession vs = v.getSessionMap().get(volunteerSession.getSessionId());
        vs.setLocked(volunteerSession.isLocked());
        vs.setWorked(volunteerSession.isWorked());
        vs.setTokens(volunteerSession.getTokens());
        vs.setComment(volunteerSession.getComment());
        v.reassign(area, session);
        volunteerRepository.saveAndFlush(v);
    }

    public void saveVolunteerSession(int volunteerId, List<SessionSelectorDTO> sessions) {
        Volunteer v = volunteerRepository.findOne(volunteerId);
        for (SessionSelectorDTO session: sessions) {
            Session s = sessionRepository.findOne(session.getSessionId());
            v.addSession(s, assignableAreaRepository.getUnassigned());
        }
        volunteerRepository.saveAndFlush(v);
    }

    /**
     * Convert the Sorting and Paging components of a Vaadin Query into a Spring JPA PageRequest
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

        private VolunteerAreaAssigner(Volunteer volunteer) {
            this.volunteer = volunteer;
        }

        public void assign(Collection<AreaSelectorDTO> areas) {
            areas.forEach(this::assignToFormArea);
        }

        private void assignToFormArea(AreaSelectorDTO areaSelectorDTO) {
            assignableAreaRepository.findByFormAreaId(areaSelectorDTO.getAreaId()).forEach(area -> {
                if (areaSelectorDTO.getPreference()==Preference.No) {
                    volunteer.removeArea(area, assignableAreaRepository.getUnassigned());
                } else {
                    volunteer.addArea(area, areaSelectorDTO.getPreference());
                }
            });
            volunteer.addArea(assignableAreaRepository.getNotWorking(), Preference.DontMind);
            volunteer.addArea(assignableAreaRepository.getUnassigned(), Preference.DontMind);
        }

    }
}
