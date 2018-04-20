package org.camra.staffing.data.service;

import org.camra.staffing.controller.FormDTO;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class FormService {

    @Autowired private SessionService sessionService;
    @Autowired private AssignedCountsService assignedCountsService;
    @Autowired private VolunteerService volunteerService;

    public VolunteerDTO populateDTO(FormDTO formDTO, Optional<VolunteerDTO> existingVolunteer) {
        VolunteerDTO dto = existingVolunteer.orElseGet(VolunteerDTO::new);
        dto.setId(formDTO.id);
        dto.setForename(formDTO.forename);
        dto.setSurname(formDTO.surname);
        dto.setEmail(formDTO.email);
        dto.setMembership(formDTO.membership);
        dto.setManagervouch(formDTO.managervouch);
        dto.setSia(formDTO.sia);
        dto.setCellar(formDTO.cellar);
        dto.setFirstaid(formDTO.firstAid);
        dto.setForklift(formDTO.forkLift);
        dto.setInstructions(true);
        dto.setAreas(formDTO.areas);
        dto.setUuid(formDTO.uuid);
        dto.setComment(formDTO.comment);
        dto.setInstructions(formDTO.instructions);

        for (SessionSelectorDTO session : formDTO.sessionList()) {
            if (session.isSelected()) {
                dto.addSession(session.getSessionId());
            } else {
                dto.removeSession(session.getSessionId());
            }
        }
        return dto;
    }

    public FormDTO populateDTO(Optional<VolunteerDTO> volunteerDTO) {
        FormDTO dto = new FormDTO();
        int volunteerId = volunteerDTO.map(VolunteerDTO::getId).orElse(0);

        dto.id = volunteerDTO.map(VolunteerDTO::getId).orElse(null);
        dto.uuid = volunteerDTO.map(VolunteerDTO::getUuid).orElse(null);
        dto.forename = volunteerDTO.map(VolunteerDTO::getForename).orElse(null);
        dto.surname = volunteerDTO.map(VolunteerDTO::getSurname).orElse(null);
        dto.email = volunteerDTO.map(VolunteerDTO::getEmail).orElse(null);
        dto.membership = volunteerDTO.map(VolunteerDTO::getMembership).orElse(null);
        dto.managervouch = volunteerDTO.map(VolunteerDTO::getManagervouch).orElse(null);
        dto.comment = volunteerDTO.map(VolunteerDTO::getComment).orElse(null);
        dto.verified = volunteerDTO.map(VolunteerDTO::isVerified).orElse(false);

        dto.firstAid = volunteerDTO.map(VolunteerDTO::isFirstaid).orElse(false);
        dto.sia = volunteerDTO.map(VolunteerDTO::isSia).orElse(false);
        dto.forkLift = volunteerDTO.map(VolunteerDTO::isForklift).orElse(false);
        dto.cellar = volunteerDTO.map(VolunteerDTO::isCellar).orElse(false);

        dto.areas = areasForVolunteer(volunteerId);

        dto.comment = volunteerDTO.map(VolunteerDTO::getComment).orElse(null);
        dto.instructions = volunteerDTO.map(VolunteerDTO::isInstructions).orElse(false);

        List<SessionSelectorDTO> sessions = getSessionsWithAmounts(volunteerId);
        dto.sessions = sessions.stream().collect(groupingBy(s -> s.getSessionDate(), LinkedHashMap::new, toList()));
        return dto;
    }

    public void populateMember(CamraMember member, FormDTO dto) {
        dto.forename = member.getForename();
        dto.surname = member.getSurname();
        dto.email = member.getEmail();
        dto.membership = member.getMembership();
    }

    private List<AreaSelectorDTO> areasForVolunteer(int volunteerId) {
        return volunteerService.getAreaSelectors(Optional.of(volunteerId));
    }

    private List<SessionSelectorDTO> getSessionsWithAmounts(int volunteerId) {
        Map<Integer, List<AssignedCountsDTO>> counts = assignedCountsService.getCountsBySession();
        List<SessionSelectorDTO> sessions = getSessions(volunteerId);
        for (SessionSelectorDTO session : sessions) {

            List<AssignedCountsDTO> countsForSession = counts.get(session.getSessionId());
            session.setTotalAssigned(countsForSession.stream().mapToInt(AssignedCountsDTO::getAssigned).sum());
            session.setTotalRequired(countsForSession.stream().mapToInt(AssignedCountsDTO::getRequired).sum());
        }
        return sessions;
    }

    private List<SessionSelectorDTO> getSessions(int volunteerId) {
        if (volunteerId==0) {
            return sessionService.getSessionsForForm();
        } else {
            return volunteerService.getPossibleSessions(volunteerId);
        }
    }

}
