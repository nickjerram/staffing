package org.camra.staffing.data.entity;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;

@Entity
@Cacheable(false)
@Table(name="area_selector")
public class AreaSelector extends StaffingEntity<AreaSelector.ID> {

	@EmbeddedId @Getter ID id;
	@Getter String name;
	
	@Enumerated(EnumType.ORDINAL)
	@Getter Preference preference;

	@Embeddable @Data
	public static class ID implements Serializable {
		private Integer volunteerId;
		private Integer areaId;
	}
}