package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.entity.VolunteerSession;
import org.hibernate.validator.constraints.Email;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.*;

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

    public VolunteerDTO() {
        id = 0;
    }

    public VolunteerDTO(Volunteer volunteer) {
        BeanUtils.copyProperties(volunteer, this);
        totalSessions = volunteer.getSessions().size();
        for (VolunteerSession session : volunteer.getSessions()) {
            if (session.getArea().getId()!=-1) assignedSessions++;
            if (session.isWorked()) worked++;
        }
    }

    public boolean equals(Object o) {
        return (o instanceof VolunteerDTO) && ((VolunteerDTO)o).id == this.id;
    }

    public int hashCode() {
        return id;
    }


}
