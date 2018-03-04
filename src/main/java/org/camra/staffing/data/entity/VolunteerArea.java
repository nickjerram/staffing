package org.camra.staffing.data.entity;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="volunteer_area")
@IdClass(VolunteerArea.ID.class)
@Cacheable(false)
@EqualsAndHashCode(callSuper=false, of={"volunteerId","areaId"})
public class VolunteerArea extends StaffingEntity<VolunteerArea.ID> {

	public VolunteerArea() {}
	
	VolunteerArea(Volunteer v, AssignableArea a) {
		volunteerId = v.getId();
		volunteer = v;
		areaId = a.getId();
		assignableArea = a;
	}
	
	VolunteerArea(Volunteer v, AssignableArea a, Preference preference) {
		volunteerId = v.getId();
		volunteer = v;
		areaId = a.getId();
		assignableArea = a;
		this.preference = preference;
	}
	
	@Id
	private Integer volunteerId ;
	
	@Id
	private Integer areaId;
	
	public ID getId() {
		return new ID(this.volunteerId, this.areaId);
	}

	@ManyToOne @Getter
	@JoinColumn(name = "volunteerId", updatable = false, insertable = false, referencedColumnName = "id")
	private Volunteer volunteer;

	@ManyToOne @Getter
	@JoinColumn(name = "areaId", updatable = false, insertable = false, referencedColumnName = "id")
	private AssignableArea assignableArea;

    @Getter @Setter
    @Enumerated(EnumType.ORDINAL)
    private Preference preference;
        
    @Data
    public static class ID implements Serializable {
		private static final long serialVersionUID = -5479977574198623226L;
		private ID(int v, int a) {
			this.volunteerId = v;
			this.areaId = a;
		}
		private int volunteerId ;
    	private int areaId;    	
    }
    

}