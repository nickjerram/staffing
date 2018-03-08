package org.camra.staffing.data.entity;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Cacheable(false)
@Table(name="possible_session")
public class PossibleSession {

    @EmbeddedId @Getter ID id;
    private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date start;
    private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date finish;
    private @Getter @Column(length = 50) String name;
    private @Getter boolean setup;
    private @Getter boolean open;
    private @Getter boolean takedown;
    private @Getter boolean night;

    private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date volunteerStart;
    private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date volunteerFinish;
    private @Getter boolean assigned;

    @Embeddable @Data
    public static class ID implements Serializable {
        private Integer volunteerId;
        private Integer sessionId ;
    }

}
