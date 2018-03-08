package org.camra.staffing.data.entityviews;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Cacheable(false)
@Table(name="view_volunteer_session")
public class VolunteerSessionView {

    @EmbeddedId private ID id;
    private String forename;
    private String surname;
    private String sessionName;
    private Integer areaId;
    private String areaName;
    private boolean locked;
    private @Temporal(value = TemporalType.TIMESTAMP) Date start;
    private @Temporal(value = TemporalType.TIMESTAMP) Date finish;
    private @Temporal(value = TemporalType.TIMESTAMP) Date sessionStart;
    private @Temporal(value = TemporalType.TIMESTAMP) Date sessionFinish;
    private String comment;
    private boolean worked;
    private int tokens;
    private int assigned;
    private int required;

    @Embeddable @Getter
    public static class ID implements Serializable {
        private Integer volunteerId;
        private Integer sessionId;
    }
}
