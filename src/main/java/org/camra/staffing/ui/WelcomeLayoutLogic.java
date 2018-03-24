package org.camra.staffing.ui;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class WelcomeLayoutLogic extends WelcomeLayout {

    @Value("${staffing.festivalName}") private String festivalName;
    @Value("${staffing.message}") private String sideMessage;

    @PostConstruct
    private void init() {
        sideTitle.setValue(festivalName);
        sideText.setValue(sideMessage);
    }

    public void setSideTitle(String title) {
        sideTitle.setValue(title);
    }

    public void setSideText(String text) {
        sideText.setValue(text);
    }

    public void setSizeLarge() {
        panel.setWidth("80%");
        panel.setHeight("80%");
    }

    public void setContent(Component component) {
        panel.setContent(component);
    }
}
