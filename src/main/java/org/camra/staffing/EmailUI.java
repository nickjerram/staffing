package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.WelcomeLayoutLogic;
import org.camra.staffing.ui.volunteer.forms.EmailFormLogic;
import org.camra.staffing.ui.volunteer.message.MessageView;
import org.camra.staffing.ui.volunteer.message.MessageViewLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/email")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class EmailUI extends UI {

    @Autowired private WelcomeLayoutLogic welcomeLayout;
    @Lazy @Autowired private EmailFormLogic emailForm;
    @Autowired private MessageViewLogic messageView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");
        welcomeLayout.setContent(emailForm);
        setContent(welcomeLayout);
        emailForm.show();
    }

    public void showMessage(String title, String message) {
        messageView.setMessage(message);
        messageView.setTitle(title);
        welcomeLayout.setContent(messageView);
    }
}
