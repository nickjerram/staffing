package org.camra.staffing.data.entity;

import lombok.Getter;
import org.hibernate.mapping.Collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Preference {

    No("No"),DontMind("Don't Mind"),Yes("Yes");

	@Getter private String caption;

	Preference(String caption) {
	    this.caption = caption;
    }

    public static final List<Preference> list = Arrays.asList(Preference.values());

}
