package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entity.VolunteerArea;

@Setter @ToString
public class AssignmentSelectorDTO {

    @Getter private int areaId;
    @Getter private String areaName;
    @Getter private int staffAssigned;
    @Getter private int staffRequired;
    @Getter private Preference areaPreference;
    @Getter private boolean current;
    private boolean anyone;

    public static AssignmentSelectorDTO populateFields(VolunteerArea volunteerArea) {
        AssignmentSelectorDTO dto = new AssignmentSelectorDTO();
        dto.areaId = volunteerArea.getAssignableArea().getId();
        dto.areaName = volunteerArea.getAssignableArea().getName();
        dto.areaPreference = volunteerArea.getPreference();
        dto.anyone = volunteerArea.getAssignableArea().isAnyone();
        return dto;
    }

}

