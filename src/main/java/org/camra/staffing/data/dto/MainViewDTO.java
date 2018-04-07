package org.camra.staffing.data.dto;

import lombok.Data;
import lombok.ToString;
import org.camra.staffing.data.entityviews.MainView;
import org.springframework.beans.BeanUtils;

@Data
@ToString
public class MainViewDTO {

    private int id;
    private int volunteerId;
    private String volunteerName;
    private int sessionId;
    private String sessionName;
    private int areaId;
    private String areaName;
    private boolean yesArea;
    private int currentAreaId;
    private String currentAreaName;
    private boolean current;
    private int assigned;
    private int worked;
    private int required;
    private boolean locked;
    private boolean volunteerWorked;
    private int tokens;
    private String comment;

    public static MainViewDTO create(MainView mainView) {
        MainViewDTO dto = new MainViewDTO();
        BeanUtils.copyProperties(mainView, dto);
        return dto;
    }

}
