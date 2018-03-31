package org.camra.staffing.data.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name="form_area")
@EqualsAndHashCode(callSuper=false, of={"id"})
public class FormArea {

	@Id @Getter private Integer id;
	private @Getter @Column(length = 50) String name;
	private @Getter boolean dontmind;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="formArea", cascade = { CascadeType.MERGE, CascadeType.PERSIST }) 
	private List<AssignableArea> assignableAreas = new ArrayList<AssignableArea>();
	
	public List<AssignableArea> getAssignableAreas() {
		return Collections.unmodifiableList(assignableAreas);
	}

}
