package org.camra.staffing.util;

import lombok.ToString;

@ToString
public class CamraMember {

    private String surname;
    private String forename;
    private String email;
    private String number;

    public CamraMember(String surname, String forename, String email, String membership) {
        this.surname = surname;
        this.forename = forename;
        this.email = email;
        this.number = membership;
    }

    public String getSurname() {
        return surname;
    }

    public String getForename() {
        return forename;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }


}
