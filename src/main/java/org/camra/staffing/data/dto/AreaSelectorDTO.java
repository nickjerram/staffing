package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.AreaSelector;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entity.VolunteerArea;

/**
 * Used on VolunteerForm for selecting area preferences
 */
@Getter
@Setter
@ToString
public class AreaSelectorDTO {

    private int areaId;
    private String areaName;
    private Preference preference;

    public Integer getId() {
        return areaId;
    }

    public static AreaSelectorDTO create(AreaSelector as) {
        AreaSelectorDTO dto = new AreaSelectorDTO();
        dto.areaId = as.getId().getAreaId();
        dto.areaName = as.getName();
        dto.preference = as.getPreference();
        return dto;
    }

    public static AreaSelectorDTO fromVolunteerArea(VolunteerArea va) {
        AreaSelectorDTO dto =  new AreaSelectorDTO();
        dto.setAreaId(va.getAssignableArea().getId());
        dto.setAreaName(va.getAssignableArea().getName());
        dto.setPreference(va.getPreference());
        return dto;
    }
}
