package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entity.VolunteerArea;
import org.camra.staffing.data.entityviews.AssignmentSelectorView;
import org.springframework.beans.BeanUtils;

@Setter @ToString
public class AssignmentSelectorDTO {

    @Getter private int areaId;
    @Getter private String name;
    @Getter private int assigned;
    @Getter private int required;
    @Getter private Preference preference;
    @Getter private boolean selected;
    private boolean anyone;

    public static AssignmentSelectorDTO create(AssignmentSelectorView selectorView) {
        AssignmentSelectorDTO dto = new AssignmentSelectorDTO();
        BeanUtils.copyProperties(selectorView, dto);
        dto.areaId = selectorView.getId().getAreaId();
        return dto;
    }

    public static AssignmentSelectorDTO populateFields(VolunteerArea volunteerArea) {
        AssignmentSelectorDTO dto = new AssignmentSelectorDTO();
        dto.areaId = volunteerArea.getAssignableArea().getId();
        dto.name = volunteerArea.getAssignableArea().getName();
        dto.preference = volunteerArea.getPreference();
        dto.anyone = volunteerArea.getAssignableArea().isAnyone();
        return dto;
    }

    public void removeAssignment() {
        this.assigned--;
        this.selected = false;
    }

    public void addAssignment() {
        this.assigned++;
        this.selected = true;
    }

}

