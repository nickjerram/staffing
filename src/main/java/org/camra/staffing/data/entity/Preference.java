package org.camra.staffing.data.entity;

import lombok.Getter;

public enum Preference {
	
	No("No"),DontMind("Don't Mind"),Yes("Yes");

	@Getter private String caption;

	Preference(String caption) {
	    this.caption = caption;
    }

}
