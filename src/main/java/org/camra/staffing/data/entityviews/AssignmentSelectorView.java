package org.camra.staffing.data.entityviews;

import lombok.Getter;
import org.camra.staffing.data.entity.Preference;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Cacheable(false)
@Table(name="view_assignment_selector")
public class AssignmentSelectorView {

    @EmbeddedId private ID id;
    private String name;
    @Enumerated(EnumType.ORDINAL) private Preference preference;
    private int assigned;
    private int required;
    private boolean selected;


    @Embeddable @Getter
    public static class ID implements Serializable {
        private Integer volunteerId;
        private Integer sessionId;
        private Integer areaId;
    }

}
