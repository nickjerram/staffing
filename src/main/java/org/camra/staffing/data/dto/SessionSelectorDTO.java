package org.camra.staffing.data.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.entityviews.PossibleSession;
import org.springframework.beans.BeanUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@EqualsAndHashCode(of="sessionId")
public class SessionSelectorDTO {

    private static DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private Integer sessionId;
    private String sessionName;
    private String description;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime finishTime;
    private LocalTime volunteerStartTime;
    private LocalTime volunteerFinishTime;
    private boolean selected;
    private int totalAssigned;
    private int totalRequired;
    private boolean night;

    public String getStyle() {
        if (totalRequired<1) {
            return "levelX";
        } else {
            double ratio = (double) totalAssigned / (double) totalRequired;
            int scale = (int) (10.0*Math.pow(ratio,3));
            return scale>9 ? "levelA" : "level"+scale;
        }

    }

    public static SessionSelectorDTO create(Session session) {
        SessionSelectorDTO dto = new SessionSelectorDTO();
        dto.sessionId = session.getId();
        dto.sessionName = session.getName();
        dto.sessionDate = toDate(session.getStart());
        dto.startTime = toTime(session.getStart());
        dto.finishTime = toTime(session.getFinish());
        dto.description = dto.startTime.format(HHMM)+"-"+dto.finishTime.format(HHMM);
        dto.night = session.isNight();
        dto.selected = false;
        return dto;
    }

    public static SessionSelectorDTO create(PossibleSession ps) {
        SessionSelectorDTO dto = new SessionSelectorDTO();
        dto.sessionId = ps.getId().getSessionId();
        dto.sessionName = ps.getName();
        dto.sessionDate = toDate(ps.getStart());
        dto.startTime = toTime(ps.getStart());
        dto.finishTime = toTime(ps.getFinish());
        dto.volunteerStartTime = toTime(ps.getVolunteerStart());
        dto.volunteerFinishTime = toTime(ps.getVolunteerFinish());
        dto.description = dto.startTime.format(HHMM)+"-"+dto.finishTime.format(HHMM);
        dto.selected = ps.isAssigned();
        return dto;
    }

    private static LocalTime toTime(Date date) {
        return date==null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    private static LocalDate toDate(Date date) {
        return date==null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }
}
