package org.camra.staffing.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="volunteer_session")
@Cacheable(false)
@EqualsAndHashCode(callSuper=false, of={"id"})
public class VolunteerSession extends StaffingEntity<VolunteerSession.ID> {
	
	public VolunteerSession() {}

	VolunteerSession(Volunteer v, Session s) {
		this.volunteer = v;
		this.session = s;
		this.id = new ID();
		this.id.sessionId = s.getId();
		this.id.volunteerId = v.getId();
	}

	VolunteerSession(VolunteerArea va, Session s) {
		this.volunteer = va.getVolunteer();
		this.session = s;
		this.id = new ID();
		this.id.sessionId = s.getId();
		this.id.volunteerId = va.getVolunteer().getId();

		this.volunteerArea = va;
	}
	
	@EmbeddedId @Getter ID id;
	
	@ManyToOne
	@Getter
	@JoinColumn(name = "volunteerId", referencedColumnName = "id", insertable=false, updatable=false)
	private Volunteer volunteer;

	@ManyToOne
	@Getter
	@JoinColumn(name = "sessionId", referencedColumnName = "id", insertable=false, updatable=false)
	private Session session;

	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "volunteerId", referencedColumnName="volunteerId", insertable=false, updatable=false),
		@JoinColumn(name = "areaId", referencedColumnName="areaId"),
	})
	private VolunteerArea volunteerArea;

	private @Getter @Setter boolean locked;
	private @Getter @Setter boolean worked;
	private @Getter @Setter int tokens;
	private @Getter @Setter String comment;
	private @Getter @Setter @Temporal(value = TemporalType.TIMESTAMP) Date start;
	private @Getter @Setter @Temporal(value = TemporalType.TIMESTAMP) Date finish;
	
	public void setArea(AssignableArea area) {
		VolunteerArea newVolunteerArea = new VolunteerArea(volunteer, area);
		newVolunteerArea.setPreference(volunteerArea.getPreference());
		volunteerArea = newVolunteerArea;
	}
	
	public AssignableArea getArea() {
		return volunteerArea.getAssignableArea();
	}
		
    @Data @Embeddable
    @EqualsAndHashCode(of={"volunteerId","sessionId"})
    public static class ID implements Serializable {
		private static final long serialVersionUID = -8180952225755752806L;
		private int volunteerId ;
    	private int sessionId;    	
    }

}