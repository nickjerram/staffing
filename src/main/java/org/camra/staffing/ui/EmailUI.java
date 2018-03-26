package org.camra.staffing.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.camra.staffing.widgets.volunteer.forms.EmailFormLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/email")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class EmailUI extends StaffingUI {

    @Lazy @Autowired private EmailFormLogic emailForm;

    protected Component content() {
        emailForm.show();
        return emailForm;
    }

}
