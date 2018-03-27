package org.camra.staffing.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.camra.staffing.widgets.volunteer.forms.ApplicationFormLogic;
import org.camra.staffing.widgets.volunteer.message.MessageViewLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;

@SpringUI(path="/form/*")
@Theme("mytheme")
@Viewport("width=device-width, initial-scale=1")
public class ApplicationFormUI extends StaffingUI {

    @Value("${mainform.notfound}") private String notFound;
    @Autowired private WelcomeLayoutLogic welcome;
    @Lazy @Autowired private ApplicationFormLogic applicationFormLogic;
    @Autowired private MessageViewLogic messageView;
    @Autowired private VolunteerService volunteerService;

    @Override
    protected Component content() {
        String[] parts = getPage().getLocation().toString().split("/");
        String uuid = parts[parts.length-1];
        Optional<VolunteerDTO> volunteer = volunteerService.getVolunteer(uuid);
        volunteer.ifPresent(this::foundVolunteer);
        if (!volunteer.isPresent()) {
            showMessage("Sorry...", notFound);
        }
        applicationFormLogic.setMargin(isMobile());
        return applicationFormLogic;
    }

    private void foundVolunteer(VolunteerDTO volunteer) {
        applicationFormLogic.setVolunteer(volunteer);
        if (!isMobile()) {
            welcome.setSizeLarge();
        }
    }

    public void showMessage(String title, String message) {
        messageView.setMessage(message);
        messageView.setTitle(title);
        welcome.setContent(messageView);
    }


}
