package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.entity.VolunteerSession;
import org.hibernate.validator.constraints.Email;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean instructions;
    private boolean firstaid;
    private boolean sia;
    private boolean cellar;
    private boolean forklift;
    private boolean other;
    private boolean confirmed;
    private String comment;
    private int assignedSessions;
    private int totalSessions;
    private int worked;
    private Map<Integer,AreaSelectorDTO> areas = new HashMap<>();

    public VolunteerDTO() {
        id = 0;
    }

    /**
     * Construct a DTO from the specified Volunteer
     * @param volunteer
     */
    public VolunteerDTO(Volunteer volunteer) {
        BeanUtils.copyProperties(volunteer, this);
        totalSessions = volunteer.getSessions().size();
        for (VolunteerSession session : volunteer.getSessions()) {
            if (session.getArea().getId()!=-1) assignedSessions++;
            if (session.isWorked()) worked++;
        }
    }

    /**
     * Populate the Volunteer with the fields in this DTO
     * @param volunteer
     */
    public void populateVolunteer(Volunteer volunteer) {
        BeanUtils.copyProperties(this, volunteer);
    }

    public void setAreas(List<AreaSelectorDTO> areaList) {
        areas.clear();
        areaList.forEach(area -> areas.put(area.getId(), area));
    }

    public void addArea(AreaSelectorDTO area) {
        areas.put(area.getAreaId(), area);
    }

    public boolean equals(Object o) {
        return (o instanceof VolunteerDTO) && ((VolunteerDTO)o).id == this.id;
    }

    public int hashCode() {
        return id;
    }


}
