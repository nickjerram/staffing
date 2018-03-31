package org.camra.staffing.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="session")
@EqualsAndHashCode(callSuper=false, of={"id"})
public class Session {

	public Session() {}
	
	public Session(String name, Date start, Date finish) {
		this.name = name;
		this.start = start;
		this.finish = finish;
	}
	
	@Id @Getter private Integer id;
	private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date start;
	private @Getter @Temporal(value = TemporalType.TIMESTAMP) Date finish;
	private @Getter @Column(length = 50) String name;
	private @Getter boolean setup;
	private @Getter boolean open;
	private @Getter boolean takedown;
	private @Getter boolean special;
	private @Getter boolean night;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="session") 
	@Getter
	private List<VolunteerSession> volunteerSessions = new ArrayList<VolunteerSession>();

}
