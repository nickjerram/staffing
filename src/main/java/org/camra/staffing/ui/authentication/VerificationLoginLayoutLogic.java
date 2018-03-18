package org.camra.staffing.ui.authentication;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import org.camra.staffing.VerificationUI;
import org.camra.staffing.data.service.CamraAuthentication;
import org.camra.staffing.ui.volunteer.forms.ApplicationFormLogic;
import org.camra.staffing.util.CamraMember;
import org.camra.staffing.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Optional;

@UIScope
@SpringComponent
public class VerificationLoginLayoutLogic extends LoginLayout {

    @Autowired private Properties properties;
    @Autowired private CamraAuthentication authentication;
    @Autowired private VerificationUI volunteerUI;
    @Autowired private ApplicationFormLogic applicationFormLogic;

    @PostConstruct
    private void init() {
        title.setValue(properties.getFestivalName()+" Volunteering");
        sideLabel.setValue(properties.getMessage());
        login.addClickListener(this::verify);
    }

    private void verify(Button.ClickEvent clickEvent) {
        Optional<CamraMember> member = authentication.requestMemberDetails(fromString(username.getValue()), password.getValue());
        if (member.isPresent()) {
            applicationFormLogic.setMember(member.get());
            volunteerUI.setContent(applicationFormLogic);
        }
    }

    private int fromString(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

}
