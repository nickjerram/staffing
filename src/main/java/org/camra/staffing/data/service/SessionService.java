package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.entity.VolunteerSession;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.camra.staffing.data.repository.SessionRepository;
import org.camra.staffing.data.repository.VolunteerSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class SessionService {

    @Autowired private SessionRepository sessionRepository;
    @Autowired private AssignedCountsService assignedCountsService;
    @Autowired private VolunteerSessionRepository volunteerSessionRepository;

    public List<SessionDTO> getSessions() {
        return sessionRepository.findAllByOrderByStartAsc().stream().map(SessionDTO::create).collect(toList());
    }

    public List<SessionSelectorDTO> getSessionsForForm() {
        return sessionRepository.findAllByOrderByStartAsc().stream().map(SessionSelectorDTO::create).collect(toList());
    }

    public Stream<SessionDTO> getSessions(Specification<Session> specification, Pageable pageable) {
        return sessionRepository.findAll(specification, pageable).getContent().stream().map(SessionDTO::create);
    }

    public Stream<VolunteerSessionDTO> getAssignments(Specification<VolunteerSessionView> specification, Pageable pageable) {
        return volunteerSessionRepository.findAll(specification, pageable).getContent().stream().map(VolunteerSessionDTO::create);
    }

    public int getAssignmentCount(Specification<VolunteerSessionView> specification) {
        return (int) volunteerSessionRepository.count(specification);
    }

    public int getSessionCount(Specification<Session> specification) {
        return (int) sessionRepository.count(specification);
    }

    public Map<LocalDate, List<SessionDTO>> getSessionMap() {
        return sessionRepository.findAllByOrderByStartAsc().stream()
                .map(SessionDTO::create).collect(groupingBy(s->s.getDay(), LinkedHashMap::new, toList()));
    }

    public Stream<SessionSelectorDTO> getSessionsWithAmounts(Specification<Session> specification, Pageable page) {
        Map<Integer, List<AssignedCountsDTO>> counts = assignedCountsService.getCountsBySession();
        List<SessionSelectorDTO> sessions = sessionRepository.findAll(specification, page)
                .getContent().stream().map(SessionSelectorDTO::create).collect(toList());
        sessions.forEach(session  -> {
            List<AssignedCountsDTO> countsForSession = counts.get(session.getSessionId());
            session.setTotalAssigned(countsForSession.stream().mapToInt(AssignedCountsDTO::getAssigned).sum());
            session.setTotalRequired(countsForSession.stream().mapToInt(AssignedCountsDTO::getRequired).sum());
        });

        return sessions.stream();
    }

    public List<VolunteerSessionDTO> getVolunteers(int sessionId, Query<VolunteerSessionDTO,Void> query) {
        OffsetBasedPageRequest pageRequest = new OffsetBasedPageRequest(query.getOffset(), query.getLimit());
        List<VolunteerSessionView> volunteerSessions = volunteerSessionRepository.findByIdSessionId(sessionId, pageRequest);
        return volunteerSessions.stream().map(VolunteerSessionDTO::create).collect(Collectors.toList());
    }


    public int countVolunteers(int sessionId) {
        return (int) volunteerSessionRepository.countByIdSessionId(sessionId);
    }


}
