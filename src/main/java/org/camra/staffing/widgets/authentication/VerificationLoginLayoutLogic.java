package org.camra.staffing.widgets.authentication;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.CamraAuthentication;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.VerificationUI;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.camra.staffing.widgets.volunteer.forms.ApplicationFormLogic;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Optional;

@UIScope
@SpringComponent
public class VerificationLoginLayoutLogic extends LoginLayout {

    @Value("${verification.title}") private String verificationTitle;
    @Value("${verification.message}") private String verificationMessage;
    @Value("${verification.username}") private String usernamePrompt;
    @Value("${verification.password}") private String passwordPrompt;
    @Value("${verification.failure}") private String verificationFailure;
    @Autowired private CamraAuthentication authentication;
    @Autowired private WelcomeLayoutLogic welcomeLayout;
    @Autowired private ApplicationFormLogic applicationFormLogic;
    @Autowired private VolunteerService volunteerService;
    @Autowired private VerificationUI verificationUI;

    @PostConstruct
    private void init() {
        title.setValue(verificationTitle);
        message.setValue(verificationMessage);
        username.setCaption(usernamePrompt);
        password.setCaption(passwordPrompt);
        login.addClickListener(this::verify);
    }

    private void verify(Button.ClickEvent clickEvent) {
        Optional<CamraMember> member = authentication.requestMemberDetails(fromString(username.getValue()), password.getValue());
        if (member.isPresent()) {
            Optional<VolunteerDTO> optionalVolunteer = volunteerService.getVolunteer(member.get());
            if (optionalVolunteer.isPresent()) {
                VolunteerDTO volunteerDTO = optionalVolunteer.get();
                volunteerDTO.setRetrieved(true);
                applicationFormLogic.setVolunteer(volunteerDTO);
            } else {
                VolunteerDTO newVolunteer = new VolunteerDTO();
                BeanUtils.copyProperties(member.get(), newVolunteer);
                applicationFormLogic.setVolunteer(newVolunteer);
            }
            verificationUI.setFormComponent(applicationFormLogic);
        } else {
            error.setVisible(true);
            error.setValue(verificationFailure);
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
