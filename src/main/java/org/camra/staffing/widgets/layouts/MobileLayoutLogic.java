package org.camra.staffing.widgets.layouts;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;
import org.camra.staffing.widgets.MobileLayout;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;


@SpringComponent
@UIScope
public class MobileLayoutLogic extends MobileLayout implements StaffingLayout {

    @Value("${staffing.welcomeMessage}") private String welcomeMessage;

    @PostConstruct
    private void init() {
        welcomeLabel.setValue(welcomeMessage);
    }

    public void setContent(Component component) {
        contentPanel.setContent(component);
    }
}
