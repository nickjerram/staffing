package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.volunteer.forms.ApplicationFormLogic;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUI(path="/form")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class FormUI extends UI {

    @Autowired private ApplicationFormLogic applicationFormLogic;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");
        setContent(applicationFormLogic);
    }

}
