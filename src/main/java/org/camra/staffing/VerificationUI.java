package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.authentication.VerificationLoginLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class VerificationUI extends UI {

    @Lazy @Autowired private VerificationLoginLayoutLogic verification;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");
        setContent(verification);
    }

}
