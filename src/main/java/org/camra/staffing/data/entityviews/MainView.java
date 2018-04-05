package org.camra.staffing.data.entityviews;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Cacheable(false)
@Table(name="main_view")
public class MainView {

    @Id private Integer id;
    private int volunteerId;
    private String forename;
    private String surname;
    private int sessionId;
    @Column(name="session_name") private String sessionName;
    private int areaId;
    @Column(name="area_name") private String areaName;
    private boolean current;
    private int assigned;
    private int worked;
    private int required;
    private double requiredRatio;
    private double workedRatio;

    public String toString() {
        return ""+id+" "+surname+" "+forename;
    }

}
