package org.camra.staffing.controller;

import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.entity.Preference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDTO {

    public ResponseDTO response = new ResponseDTO();
    public boolean mainForm;
    public boolean verificationForm;
    public boolean emailForm;
    public boolean submit;

    public Integer id;
    public String forename;
    public String surname;
    public String membership;
    public String managervouch;
    public String password;
    public String email;
    public String confirmEmail;
    public String captcha;
    public String uuid;
    public boolean verified;

    public boolean firstAid;
    public boolean sia;
    public boolean cellar;
    public boolean forkLift;
    public boolean instructions;

    public List<AreaSelectorDTO> areas = new ArrayList<>();

    public Map<LocalDate, List<SessionSelectorDTO>> sessions = new HashMap<>();

    public String comment;

    @SuppressWarnings("unused")
    public boolean isAreasValid() {
        for (AreaSelectorDTO area : areas) {
            if (area.getPreference()!= Preference.No) return true;
        }
        return false;
    }

    public List<SessionSelectorDTO> sessionList() {
        List<SessionSelectorDTO> sessionList = new ArrayList<>();
        for (List<SessionSelectorDTO> sessionOnDay : sessions.values()) {
            for (SessionSelectorDTO session : sessionOnDay) {
                sessionList.add(session);
            }
        }
        return sessionList;
    }


}
