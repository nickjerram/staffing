package org.camra.staffing.data.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camra.staffing.data.entity.PossibleSession;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@EqualsAndHashCode(of="sessionId")
public class SessionSelectorDTO {

    private Integer sessionId;
    private String sessionName;
    private LocalTime startTime;
    private LocalTime finishTime;
    private LocalTime volunteerStartTime;
    private LocalTime volunteerFinishTime;
    private boolean selected;

    public static SessionSelectorDTO create(PossibleSession ps) {
        SessionSelectorDTO dto = new SessionSelectorDTO();
        dto.sessionId = ps.getId().getSessionId();
        dto.sessionName = ps.getName();
        dto.startTime = toTime(ps.getStart());
        dto.finishTime = toTime(ps.getFinish());
        dto.volunteerStartTime = toTime(ps.getVolunteerStart());
        dto.volunteerFinishTime = toTime(ps.getVolunteerFinish());
        dto.selected = ps.isAssigned();
        return dto;
    }

    private static LocalTime toTime(Date date) {
        return date==null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }
}
