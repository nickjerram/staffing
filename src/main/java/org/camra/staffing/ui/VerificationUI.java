package org.camra.staffing.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import org.camra.staffing.widgets.authentication.VerificationLoginLayoutLogic;
import org.camra.staffing.widgets.layouts.StaffingLayout;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class VerificationUI extends StaffingUI {

    @Lazy @Autowired private VerificationLoginLayoutLogic verification;

    @Override
    protected Component content() {
        return verification;
    }

    public void setFormComponent(Component component) {
        StaffingLayout layout = chooseLayout();
        if (layout instanceof WelcomeLayoutLogic) {
            WelcomeLayoutLogic welcomeLayout = (WelcomeLayoutLogic) layout;
            welcomeLayout.setSizeLarge();
            welcomeLayout.setContent(component);
        } else {
            layout.setContent(component);
        }
    }

}
