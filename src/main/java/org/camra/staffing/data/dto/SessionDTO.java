package org.camra.staffing.data.dto;

import lombok.Data;
import lombok.Getter;
import org.camra.staffing.data.entity.Session;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Data
public class SessionDTO {

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
}
