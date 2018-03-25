package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.WelcomeLayoutLogic;
import org.camra.staffing.ui.volunteer.forms.ApplicationFormLogic;
import org.camra.staffing.ui.volunteer.message.MessageViewLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;

@SpringUI(path="/form/*")
@Theme("mytheme")
//@Viewport("user-scalable=no,initial-scale=0.5")
@Viewport("width=device-width, initial-scale=1")
public class ApplicationFormUI extends UI {

    @Autowired private WelcomeLayoutLogic welcome;
    @Lazy @Autowired private ApplicationFormLogic applicationFormLogic;
    @Autowired private MessageViewLogic messageView;
    @Autowired private VolunteerService volunteerService;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");

        String[] parts = getPage().getLocation().toString().split("/");
        String uuid = parts[parts.length-1];
        Optional<VolunteerDTO> volunteer = volunteerService.getVolunteer(uuid);
        volunteer.ifPresent(this::foundVolunteer);

        if (!volunteer.isPresent()) {
            showMessage("Sorry...","This URL is not valid. If you are trying to access your Staffing Form...");
        }
        setContent(welcome);
    }

    private void foundVolunteer(VolunteerDTO volunteer) {
        volunteer.setRetrieved(true);
        applicationFormLogic.setVolunteer(volunteer);
        welcome.setSizeLarge();
        welcome.setContent(applicationFormLogic);
    }

    public void showMessage(String title, String message) {
        messageView.setMessage(message);
        messageView.setTitle(title);
        welcome.setContent(messageView);
    }

}
