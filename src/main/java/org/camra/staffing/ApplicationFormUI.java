package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.WelcomeLayoutLogic;
import org.camra.staffing.ui.volunteer.forms.ApplicationFormLogic;
import org.camra.staffing.ui.volunteer.message.MessageViewLogic;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUI(path="/form/*")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class ApplicationFormUI extends UI {

    @Autowired private WelcomeLayoutLogic welcome;
    @Autowired private ApplicationFormLogic applicationFormLogic;
    @Autowired private MessageViewLogic messageView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");

        String[] parts = getPage().getLocation().toString().split("/");
        String uuid = parts[parts.length-1];
        if (applicationFormLogic.setUUID(uuid)) {
            welcome.setContent(applicationFormLogic);
        } else {
            showMessage("Sorry...","This URL is not valid. If you are trying to access your Staffing Form...");
        }
    }

    public void showMessage(String title, String message) {
        messageView.setMessage(message);
        welcome.setMainTitle(title);
        welcome.setContent(messageView);
    }

}
