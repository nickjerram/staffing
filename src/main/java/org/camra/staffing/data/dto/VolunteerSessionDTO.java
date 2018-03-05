package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.VolunteerSession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@ToString
public class VolunteerSessionDTO {

    public static final DateFormat TIME = new SimpleDateFormat("HH:mm");

    private Integer volunteerId;
    private String forename;
    private String surname;
    private Integer sessionId;
    private String sessionName;
    private Integer areaId;
    private String areaName;
    private boolean lock;
    private Date start;
    private Date finish;
    private Date sessionStart;
    private Date sessionFinish;
    private int staffAssigned;
    private int staffRequired;
    private String comment;
    private boolean worked;
    private int tokens;
    private int tokensDue;
    private String summaryTokensDue;
    private VolunteerSession.ID id;

    public static VolunteerSessionDTO create(VolunteerSession vs) {
        VolunteerSessionDTO dto = new VolunteerSessionDTO();
        dto.id = vs.getId();
        dto.volunteerId = vs.getVolunteer().getId();
        dto.forename = vs.getVolunteer().getForename();
        dto.surname = vs.getVolunteer().getSurname();
        dto.sessionId = vs.getSession().getId();
        dto.sessionName = vs.getSession().getName();
        dto.areaId = vs.getArea().getId();
        dto.areaName = vs.getArea().getName();
        dto.lock = vs.isLocked();
        dto.start = vs.getStart();
        dto.finish = vs.getFinish();
        dto.sessionStart = vs.getSession().getStart();
        dto.sessionFinish = vs.getSession().getFinish();
        dto.comment = vs.getComment();
        dto.tokens = vs.getTokens();
        dto.worked = vs.isWorked();
        return dto;
    }

    public boolean equals(Object o) {
        if (o instanceof VolunteerSessionDTO) {
            VolunteerSessionDTO other = (VolunteerSessionDTO) o;
            return other.volunteerId == this.volunteerId && other.sessionId == this.sessionId;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 997 * sessionId + volunteerId;
    }

    public VolunteerSession.ID getId() {
        return id;
    }

}
