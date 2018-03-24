package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.WelcomeLayout;
import org.camra.staffing.ui.WelcomeLayoutLogic;
import org.camra.staffing.ui.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.authentication.VerificationLoginLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class VerificationUI extends UI {

    @Lazy @Autowired private VerificationLoginLayoutLogic verification;
    @Autowired private WelcomeLayoutLogic welcomeLayout;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");
        welcomeLayout.setContent(verification);
        setContent(welcomeLayout);
    }

}
