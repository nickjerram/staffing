package org.camra.staffing.data.entityviews;

import lombok.Getter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Cacheable(false)
@Table(name="main_view")
public class MainView {

    @Id private Integer id;
    private int volunteerId;
    @Column(name="volunteer_name") private String volunteerName;
    private int sessionId;
    @Column(name="session_name") private String sessionName;
    private @Temporal(value = TemporalType.TIMESTAMP) Date start;
    private int areaId;
    @Column(name="area_name") private String areaName;
    @Column(name="yes_area")  private boolean yesArea;
    @Column(name="current_area_id") private int currentAreaId;
    @Column(name="current_area_name") private String currentAreaName;
    private boolean current;
    private int assigned;
    private int worked;
    private int required;
    private double requiredRatio;
    private double workedRatio;
    private boolean locked;
    @Column(name="volunteer_worked") private boolean volunteerWorked;
    private int tokens;
    private String comment;
    private String volunteerComment;
    private boolean firstaid;
    private boolean forklift;
    private boolean sia;
    private boolean cellar;
    private boolean hasComment;
    private boolean other;


    public String toString() {
        return ""+id+" "+volunteerName;
    }

}
