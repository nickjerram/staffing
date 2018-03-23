package org.camra.staffing.data.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class EmailDTO {

    private String forename;
    private String surname;
    @Email private String email;
    @Email private String confirmation;

    public boolean isEmailOK() {
        return email!=null && confirmation!=null && email.equals(confirmation);
    }

    public boolean isNameOK() {
        return StringUtils.hasText(forename) && StringUtils.hasText(surname);

    }
}
