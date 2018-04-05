package org.camra.staffing.data.dto;

import lombok.Getter;
import org.camra.staffing.data.entityviews.AssignedCounts;

@Getter
public class AssignedCountsDTO {

    private int areaId;
    private int sessionId;
    private int required;
    private int assigned;
    private int worked;

    public static AssignedCountsDTO create(AssignedCounts counts) {
        AssignedCountsDTO dto = new AssignedCountsDTO();

        dto.areaId = counts.getId().getAreaId();
        dto.sessionId = counts.getId().getSessionId();
        dto.required = counts.getRequired();
        dto.assigned = counts.getAssigned();
        return dto;
    }
}
