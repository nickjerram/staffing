package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.camra.staffing.data.entity.FormArea;
import org.camra.staffing.data.entityviews.AreaSelector;
import org.camra.staffing.data.entity.AssignableArea;
import org.camra.staffing.data.entity.Preference;

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
        AreaSelectorDTO dto = new AreaSelectorDTO(as.getId().getAreaId(), as.getPreference());
        dto.areaName = as.getName();
        return dto;
    }

    public static AreaSelectorDTO create(FormArea fa) {
        AreaSelectorDTO dto = new AreaSelectorDTO(fa.getId(), Preference.DontMind);
        dto.areaName = fa.getName();
        return dto;
    }

    public static AreaSelectorDTO create(AssignableArea aa) {
        AreaSelectorDTO dto = new AreaSelectorDTO(aa.getId(), Preference.DontMind);
        dto.areaName = aa.getName();
        return dto;
    }

    public AreaSelectorDTO() {}

    public AreaSelectorDTO(int areaId, Preference preference) {
        this.areaId = areaId;
        this.preference = preference;
    }

    public static final AreaSelectorDTO UNASSIGNED = new AreaSelectorDTO(AssignableArea.UNASSIGNED, Preference.DontMind);

    public static final AreaSelectorDTO NOT_NEEDED = new AreaSelectorDTO(AssignableArea.NOT_NEEDED, Preference.DontMind);

}
