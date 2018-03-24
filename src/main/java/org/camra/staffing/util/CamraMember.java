package org.camra.staffing.util;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class CamraMember {

    private String surname;
    private String forename;
    private String email;
    private String membership;
    private boolean verified = true;

    public CamraMember(String surname, String forename, String email, String membership) {
        this.surname = surname;
        this.forename = forename;
        this.email = email;
        this.membership = membership;
    }

}
