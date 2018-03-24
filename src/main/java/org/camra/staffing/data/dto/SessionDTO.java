package org.camra.staffing.data.dto;

import lombok.Data;
import org.camra.staffing.data.entity.Session;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class SessionDTO {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE dd MMM");

    private int id;
    private LocalDate day;
    private LocalTime startTime;
    private LocalTime finishTime;
    private String name;
    private boolean setup;
    private boolean open;
    private boolean takedown;
    private boolean special;
    private boolean night;

    public static SessionDTO create(Session session) {
        SessionDTO dto = new SessionDTO();
        BeanUtils.copyProperties(session, dto);
        dto.day = session.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        dto.startTime = session.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        dto.finishTime = session.getFinish().toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        return dto;
    }

    public String getDescription() {
        String text = night ? "<i>overnight</i>" :  startTime+" - "+finishTime;
        if (open) {
            text = "<b>"+text+"</b>";
        }
        return text;
    }

    public String getLongDescription() {
        String dayString = day.format(DAY_FORMAT);
        return dayString + (night ? "overnight" : startTime+" - "+finishTime);
    }
}
