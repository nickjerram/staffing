package org.camra.staffing.data.dto;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.entity.Volunteer;

public class BadgeDTO {

    @Setter private String surname;
    @Setter private String forename;
    @Getter @Setter private byte[] picture;
    @Setter private String role;

    public static BadgeDTO create(Volunteer volunteer) {
        return BadgeDTO.create(volunteer.getForename(), volunteer.getSurname(), volunteer.getRole(),
                volunteer.getPicture());
    }

    public static BadgeDTO create(String forename, String surname, String role, byte[] pic) {
        BadgeDTO dto = new BadgeDTO();
        dto.surname = surname;
        dto.forename = forename;
        dto.role = role;
        dto.picture = pic;
        return dto;
    }

    public String getName() {
        return deNull(forename)+" "+deNull(surname);
    }

    private String deNull(String s) {
        return s==null ? "" : s;
    }

    private boolean isBlank(String s) {
        return s==null || s.trim().length()==0;
    }

    public String getRole() {
        return role;
    }

    public boolean isDots() {
        return isBlank(role);
    }

    public boolean isSmallLogo() {
        return !isBlank(role);
    }

    public Color getBannerColor() {
        return isDots() ? Color.WHITE : Color.YELLOW;
    }
}

