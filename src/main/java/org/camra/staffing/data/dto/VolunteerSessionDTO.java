package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.VolunteerSession;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.springframework.beans.BeanUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private boolean locked;
    private Date start;
    private Date finish;
    private Date sessionStart;
    private Date sessionFinish;
    private int assigned;
    private int required;
    private String comment;
    private boolean worked;
    private int tokens;
    private int tokensDue;
    private String summaryTokensDue;

    public VolunteerSessionDTO() {}

    public VolunteerSessionDTO(MainViewDTO mainViewDTO) {
        this.volunteerId = mainViewDTO.getVolunteerId();
        this.sessionId = mainViewDTO.getSessionId();
        this.areaId = mainViewDTO.getCurrentAreaId();
    }

    public static VolunteerSessionDTO create(VolunteerSessionView vs) {
        VolunteerSessionDTO dto = new VolunteerSessionDTO();
        BeanUtils.copyProperties(vs, dto);
        dto.volunteerId = vs.getId().getVolunteerId();
        dto.sessionId = vs.getId().getSessionId();
        return dto;
    }

    public void setStartTime(Date startTime) {
        this.start = copyTimeOfDay(this.sessionStart, startTime);
    }

    public void setFinishTime(Date finishTime) {
        this.finish = copyTimeOfDay(this.sessionFinish, finishTime);
    }

    public String getDescription() {
        return sessionName+" ("+TIME.format(sessionStart)+"-"+TIME.format(sessionFinish)+")";
    }


    private Date copyTimeOfDay(Date ddmmyy, Date hhmm) {
        if (hhmm==null) return null;
        Calendar ddmmyyCalendar = Calendar.getInstance();
        Calendar hhmmCalendar = Calendar.getInstance();
        ddmmyyCalendar.setTime(ddmmyy);
        hhmmCalendar.setTime(hhmm);
        ddmmyyCalendar.set(Calendar.HOUR_OF_DAY, hhmmCalendar.get(Calendar.HOUR_OF_DAY));
        ddmmyyCalendar.set(Calendar.MINUTE, hhmmCalendar.get(Calendar.MINUTE));
        return new Date(ddmmyyCalendar.getTimeInMillis());
    }


}
