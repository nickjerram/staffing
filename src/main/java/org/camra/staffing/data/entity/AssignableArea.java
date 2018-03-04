package org.camra.staffing.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name="assignable_area")
@EqualsAndHashCode(callSuper=false, of={"id"})
public class AssignableArea extends StaffingEntity<Integer> {

	public AssignableArea() {}
	
	public AssignableArea(FormArea formArea, String name) {
		this.formArea = formArea;
		this.name = name;
	}
	
	@Id @Getter private Integer id;
	private @ManyToOne @Getter FormArea formArea;
	private @Getter @Column(length = 50) String name;
	private @Getter @Column(length = 10, name="short_name") String shortName;
	private @Getter boolean anyone;
	private @Getter @Column(name="public_facing") boolean publicFacing;
	private @Getter @Column int type;
	
}
