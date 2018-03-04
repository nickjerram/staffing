package org.camra.staffing.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.entity.StaffingEntity;
import org.camra.staffing.data.entity.VolunteerSession;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="volunteer")
@Cacheable(false)
@EqualsAndHashCode(callSuper=false, of={"id"})
public class Volunteer extends StaffingEntity<Integer> {

	@Id @Getter @GeneratedValue(strategy = GenerationType.AUTO) private Integer id;
	
	private @Getter @Setter @Column(length = 36) String uuid;
	private @Getter @Setter @Column(length = 40) String password;
	private @Getter @Setter @Column(length = 50) String forename;
	private @Getter @Setter @Column(length = 50) String surname;
	private @Getter @Setter @Column(length = 40) String callsign;
	private @Getter @Setter @Column(length = 40) String role;
	private @Getter @Setter @Column(length = 20) String membership;
	private @Getter @Setter @Column(length = 100) String email;
	private @Getter @Setter Boolean emailVerified;
	private @Getter @Setter Integer tshirt;
	private @Getter @Setter @Lob String comment;
	private @Getter @Setter @Column(length = 100) String managervouch;
	private @Getter @Setter Boolean camping;
	private @Getter @Setter Boolean verified;
	private @Getter @Setter Boolean instructions;
	private @Getter @Setter Boolean firstaid;
	private @Getter @Setter Boolean sia;
	private @Getter @Setter Boolean cellar;
	private @Getter @Setter Boolean forklift;
	private @Getter @Setter Boolean other;
	private @Getter @Setter Boolean confirmed;
	private @Getter @Setter @Lob @Column(length=1000000) byte[] picture;
		
	@OneToMany(fetch = FetchType.LAZY, mappedBy="volunteer", cascade = CascadeType.ALL, orphanRemoval=true) 
	private List<VolunteerSession> volunteerSessions = new ArrayList<VolunteerSession>();
	
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="volunteer", cascade = CascadeType.ALL, orphanRemoval=true) 
	private List<VolunteerArea> volunteerAreas = new ArrayList<VolunteerArea>();
	
	public List<VolunteerSession> getSessions() {
		return Collections.unmodifiableList(volunteerSessions);
	}

	public Map<Integer,VolunteerSession> getSessionMap() {
		Map<Integer,VolunteerSession> result = new HashMap<>();
		for (VolunteerSession vs : volunteerSessions) {
			result.put(vs.getSession().getId(), vs);
		}
		return Collections.unmodifiableMap(result);
	}
	
	public List<VolunteerArea> getAreas() {
		return Collections.unmodifiableList(volunteerAreas);		
	}
	
	public void clearSessions() {
		volunteerSessions.clear();
	}
	
	public void addSession(Session session, AssignableArea area) {
		for (VolunteerArea va : volunteerAreas) {
			if (va.getAssignableArea().equals(area)) {
				VolunteerSession vs = new VolunteerSession(va, session);
				volunteerSessions.add(vs);
				break;
			}
		}
		
	}
	
	public void removeSession(Session session) {
		VolunteerSession togo = new VolunteerSession(this, session);
		volunteerSessions.remove(togo);
	}
	
	public void addArea(AssignableArea area, Preference preference) {
		boolean added = false;
		for (VolunteerArea va : volunteerAreas) {
			if (va.getAssignableArea().getId()==area.getId()) {
				va.setPreference(preference);
				added = true;
			}
		}
		
		if (!added) {
			volunteerAreas.add(new VolunteerArea(this, area, preference));
		}
	}
	
	public void removeArea(AssignableArea area, AssignableArea replacement) {
		VolunteerArea va = new VolunteerArea(this, area);
		if (volunteerAreas.contains(va)) {
			for (VolunteerSession vs : volunteerSessions) {
				if (vs.getArea().equals(area)) {
					vs.setArea(replacement);
				}
			}
			volunteerAreas.remove(va);
		}
	}
	
	public void updateArea(AssignableArea area, Preference preference) {
		VolunteerArea check = new VolunteerArea(this, area);
		for (VolunteerArea va : volunteerAreas) {
			if (va.equals(check)) {
				va.setPreference(preference);
				return;
			}
		}
		addArea(area, preference);
	}
	
	public void reassign(AssignableArea area, Session session) {
		for (VolunteerSession vs : volunteerSessions) {
			if (vs.getSession().equals(session)) {
				vs.setArea(area);
			}
		}
	}
	
	public AssignableArea getAssignment(Session session) {
		for (VolunteerSession vs : volunteerSessions) {
			if (vs.getSession().equals(session)) {
				return vs.getArea();
			}
		}
		return null;
	}
	
}
