package org.camra.staffing.data.service;

import org.camra.staffing.data.dto.*;
import org.camra.staffing.data.entity.*;
import org.camra.staffing.data.entityviews.AreaSelector;
import org.camra.staffing.data.entityviews.AssignmentSelectorView;
import org.camra.staffing.data.entityviews.PossibleSession;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.camra.staffing.data.repository.*;
import org.camra.staffing.data.entity.CamraMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VolunteerService {

    @Autowired private VolunteerRepository volunteerRepository;
    @Autowired private AreaSelectorRepository areaSelectorRepository;
    @Autowired private AssignableAreaRepository assignableAreaRepository;
    @Autowired private AssignmentSelectorViewRepository assignmentSelectorViewRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private VolunteerSessionRepository volunteerSessionRepository;
    @Autowired private PossibleSessionRepository possibleSessionRepository;

    public Stream<VolunteerDTO> getVolunteers(Specification<Volunteer> specification, Pageable pageable) {
        return volunteerRepository.findAll(specification, pageable).getContent().stream().map(VolunteerDTO::create);
    }

    public int getVolunteerCount(Specification<Volunteer> specification) {
        return (int) volunteerRepository.count(specification);
    }

    public Optional<VolunteerDTO> getVolunteer(Integer id) {
        return id==null ? Optional.empty() : volunteerRepository.findById(id).map(VolunteerDTO::create);
    }

    public Optional<VolunteerDTO> getVolunteer(String uuid) {
        return volunteerRepository.findByUuid(uuid).map(VolunteerDTO::create);
    }

    public Optional<VolunteerDTO> getVolunteer(Optional<CamraMember> member) {
        return member.flatMap(this::getVolunteer);
    }

    private Optional<VolunteerDTO> getVolunteer(CamraMember member) {
        return volunteerRepository.findByMembershipAndSurnameAndForename(member.getMembership(), member.getSurname(), member.getForename())
                .map(VolunteerDTO::create);
    }

    public void setConfirmed(VolunteerDTO volunteer) {
        Volunteer toConfirm = volunteerRepository.getOne(volunteer.getId());
        toConfirm.setConfirmed(true);
        volunteerRepository.saveAndFlush(toConfirm);
    }

    public void deleteVolunteer(VolunteerDTO dto) {
        Volunteer toDelete = volunteerRepository.getOne(dto.getId());
        volunteerRepository.delete(toDelete);
    }

    public List<AreaSelectorDTO> getAreaSelectors(Optional<Integer> volunteerId) {
        int id = volunteerId.orElse(0);
        List<AreaSelector> selectors = areaSelectorRepository.findByIdVolunteerIdOrderByIdAreaId(id);
        return selectors.stream().map(AreaSelectorDTO::create).collect(Collectors.toList());
    }

    public void saveVolunteer(VolunteerDTO volunteerDTO) {
        Volunteer toSave = volunteerDTO.getId()==null ? new Volunteer() : volunteerRepository.getOne(volunteerDTO.getId());
        volunteerDTO.populateVolunteer(toSave);
        Volunteer saved = volunteerRepository.save(toSave);

        //areas
        VolunteerAreaAssigner assigner = new VolunteerAreaAssigner(saved);
        assigner.assign(volunteerDTO.getAreas().values());

        //sessions
        for (int sessionId : volunteerDTO.getSessionsToAdd()) {
            Session session = sessionRepository.getOne(sessionId);
            saved.addSession(session, assignableAreaRepository.getUnassigned());
        }
        for (int sessionId : volunteerDTO.getSessionsToRemove()) {
            Session session = sessionRepository.getOne(sessionId);
            saved.removeSession(session);
        }
        volunteerRepository.save(saved);
    }

    public List<VolunteerSessionDTO> getSessions(int volunteerId) {
        List<VolunteerSessionView> vs = volunteerSessionRepository.findByIdVolunteerIdOrderBySessionStart(volunteerId);
        for (VolunteerSessionView v : vs) {
            System.out.println(v.getSessionName());
        }
        System.out.println("============");
        //return volunteerSessionRepository.findByIdVolunteerIdOrderByStart(volunteerId)
                //.stream().map(VolunteerSessionDTO::create).collect(Collectors.toList());
        return vs.stream().map(VolunteerSessionDTO::create).collect(Collectors.toList());
    }

    public List<AssignmentSelectorDTO> getPossibleReassignments(VolunteerSessionDTO volunteerSession) {
        List<AssignmentSelectorView> selectorViews =
                assignmentSelectorViewRepository.findByIdVolunteerIdAndIdSessionId(volunteerSession.getVolunteerId(), volunteerSession.getSessionId());
        return selectorViews.stream().map(AssignmentSelectorDTO::create).collect(Collectors.toList());
    }

    public List<SessionSelectorDTO> getPossibleSessions(int volunteerId) {
        List<PossibleSession> possibleSessions = possibleSessionRepository.findByIdVolunteerIdOrderByStart(volunteerId);
        return possibleSessions.stream().map(SessionSelectorDTO::create).collect(Collectors.toList());
    }

    public void saveAssignment(VolunteerSessionDTO volunteerSession) {
        Volunteer v = volunteerRepository.getOne(volunteerSession.getVolunteerId());
        Session session = sessionRepository.getOne(volunteerSession.getSessionId());
        AssignableArea area = assignableAreaRepository.getOne(volunteerSession.getAreaId());
        VolunteerSession vs = v.getSessionMap().get(volunteerSession.getSessionId());
        vs.setLocked(volunteerSession.isLocked());
        vs.setWorked(volunteerSession.isWorked());
        vs.setTokens(volunteerSession.getTokens());
        vs.setComment(volunteerSession.getComment());
        vs.setStart(volunteerSession.getStart());
        vs.setFinish(volunteerSession.getFinish());
        v.reassign(area, session);
        volunteerRepository.saveAndFlush(v);
    }

    public void saveVolunteerSession(int volunteerId, List<Integer> sessionsIds) {
        Volunteer v = volunteerRepository.getOne(volunteerId);
        for (Integer sessionId: sessionsIds) {
            Session s = sessionRepository.getOne(sessionId);
            v.addSession(s, assignableAreaRepository.getUnassigned());
        }
        volunteerRepository.saveAndFlush(v);
    }

    public void removeVolunteerSession(int volunteerId, List<Integer> sessionIds) {
        Volunteer v = volunteerRepository.getOne(volunteerId);
        for (Integer sessionId : sessionIds) {
            Session s = sessionRepository.getOne(sessionId);
            v.removeSession(s);
        }
        volunteerRepository.saveAndFlush(v);
    }

    public void saveVolunteerPicture(int id, byte[] picture) {
        Volunteer v = volunteerRepository.getOne(id);
        v.setPicture(picture);
        volunteerRepository.save(v);
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
