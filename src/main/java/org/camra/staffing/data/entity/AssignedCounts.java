package org.camra.staffing.data.entity;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Cacheable(false)
@Table(name="assigned_counts")
public class AssignedCounts extends StaffingEntity<AssignedCounts.ID> {

	@EmbeddedId @Getter ID id;
	@Getter private int required;
	@Getter private int assigned;	
	@Getter private int worked;
	
	@Embeddable @Data
    public static class ID implements Serializable {
		private static final long serialVersionUID = -5496026683277772825L;
		private Integer areaId;    	
		private Integer sessionId ;
		
		public ID() {}
		public ID(Integer areaId, Integer sessionId) {
			this.areaId = areaId;
			this.sessionId = sessionId;
		}
    }

}
