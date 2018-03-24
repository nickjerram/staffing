package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.entity.*;
import org.hibernate.validator.constraints.Email;
import org.springframework.beans.BeanUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.constraints.*;
import java.util.*;

@Getter @Setter
public class VolunteerDTO {

    boolean expanded;
    private Integer id;
    @NotNull @Size(min = 2, message = "Include a valid Name") private String surname;
    @NotNull @Size(min = 2, message = "Include a valid Name") private String forename;
    @Email private String email;
    private String role;
    private String membership;
    private String managervouch;
    private String Uuid;
    private boolean instructions;
    private boolean firstaid;
    private boolean sia;
    private boolean cellar;
    private boolean forklift;
    private boolean other;
    private boolean confirmed;
    private boolean verified;
    private boolean emailVerified;
    private boolean camping;
    private boolean retrieved;
    private String comment;
    private int assignedSessions;
    private int totalSessions;
    private int worked;
    private Map<Integer,AreaSelectorDTO> areas = new HashMap<>();
    private Set<Integer> sessions = new HashSet<>();
    private Set<Integer> sessionsToRemove = new HashSet<>();
    private Set<Integer> sessionsToAdd = new HashSet<>();

    public void addSession(int sessionId) {
        if (!sessions.contains(sessionId)) {
            sessionsToAdd.add(sessionId);
        }
    }

    public void removeSession(int sessionId) {
        if (sessions.contains(sessionId)) {
            sessionsToRemove.add(sessionId);
        }
    }

    public VolunteerDTO() {
        areas.put(AssignableArea.UNASSIGNED, AreaSelectorDTO.UNASSIGNED);
        areas.put(AssignableArea.NOT_NEEDED, AreaSelectorDTO.NOT_NEEDED);
    }

    public static VolunteerDTO create(Volunteer volunteer) {
        VolunteerDTO dto = new VolunteerDTO();
        BeanUtils.copyProperties(volunteer, dto);
        dto.camping = volunteer.getCamping();
        dto.cellar = volunteer.getCellar();
        dto.confirmed = volunteer.getConfirmed();
        dto.emailVerified = volunteer.getEmailVerified();
        dto.firstaid = volunteer.getFirstaid();
        dto.forklift = volunteer.getForklift();
        dto.instructions = volunteer.getInstructions();
        dto.other = volunteer.getOther();
        dto.sia = volunteer.getSia();
        dto.totalSessions = volunteer.getSessions().size();
        for (VolunteerSession session : volunteer.getSessions()) {
            dto.sessions.add(session.getId().getSessionId());
            if (session.getArea().getId()!=-1) dto.assignedSessions++;
            if (session.isWorked()) dto.worked++;
        }
        for (VolunteerArea volunteerArea : volunteer.getAreas()) {
            FormArea formArea = volunteerArea.getAssignableArea().getFormArea();
            if (formArea!=null) {
                int formAreaId = formArea.getId();
                Preference preference = volunteerArea.getPreference();
                dto.areas.put(formAreaId, new AreaSelectorDTO(formAreaId, preference));
            }
        }
        return dto;
    }

    /**
     * Populate the Volunteer with the fields in this DTO
     * @param volunteer
     */
    public void populateVolunteer(Volunteer volunteer) {
        BeanUtils.copyProperties(this, volunteer);
        volunteer.setCamping(this.camping);
        volunteer.setCellar(this.cellar);
        volunteer.setConfirmed(this.confirmed);
        volunteer.setEmailVerified(this.emailVerified);
        volunteer.setFirstaid(this.firstaid);
        volunteer.setForklift(this.forklift);
        volunteer.setInstructions(this.instructions);
        volunteer.setOther(this.other);
        volunteer.setSia(this.sia);
    }

    public void setAreas(List<AreaSelectorDTO> areaList) {
        areas.clear();
        areas.put(AssignableArea.UNASSIGNED, AreaSelectorDTO.UNASSIGNED);
        areas.put(AssignableArea.NOT_NEEDED, AreaSelectorDTO.NOT_NEEDED);
        areaList.forEach(area -> areas.put(area.getId(), area));
    }

    public void addArea(AreaSelectorDTO area) {
        areas.put(area.getAreaId(), area);
    }

    public void addArea(int formAreaId, Preference preference) {
        AreaSelectorDTO areaSelector = new AreaSelectorDTO(formAreaId, preference);
        addArea(areaSelector);
    }

    public boolean equals(Object o) {
        return (o instanceof VolunteerDTO) && ((VolunteerDTO)o).id == this.id;
    }

    public int hashCode() {
        return id;
    }


}
