package org.camra.staffing.data.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="area_session")
@EqualsAndHashCode(callSuper=false, of={"id"})
public class AreaSession extends StaffingEntity<AreaSession.ID> {

	@EmbeddedId @Getter ID id;
	
	@ManyToOne @Getter
	@JoinColumn(name = "areaId", updatable = false, insertable = false, referencedColumnName = "id")
	private AssignableArea assignableArea;

	@ManyToOne @Getter
	@JoinColumn(name = "sessionId", updatable = false, insertable = false, referencedColumnName = "id")
	private Session session;

    @Getter @Setter
    private int required;	
    
    @Embeddable @Data
    @EqualsAndHashCode()
    public static class ID implements Serializable {
    	private static final long serialVersionUID = 8094659223620379450L;
    	private int areaId;
    	private int sessionId;
    }
}
