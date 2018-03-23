package org.camra.staffing.ui;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;

@SpringComponent
@UIScope
public class WelcomeLayoutLogic extends WelcomeLayout {

    private Component currentComponent;

    public void setSideTitle(String title) {
        sideTitle.setValue(title);
    }

    public void setSideText(String text) {
        sideText.setValue(text);
    }

    public void setMainTitle(String title) {
        mainTitle.setValue(title);
    }

    public void setContent(Component component) {
        if (currentComponent==null) {
            container.addComponent(component);
        } else {
            container.replaceComponent(currentComponent, component);
        }
        currentComponent = component;
    }
}
