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
    private String forename;
    private String surname;
    private int sessionId;
    private String sessionName;
    private int areaId;
    private String areaName;
    private boolean current;
    private int assigned;
    private int worked;
    private int required;

    public static MainViewDTO create(MainView mainView) {
        MainViewDTO dto = new MainViewDTO();
        BeanUtils.copyProperties(mainView, dto);
        return dto;
    }

}
