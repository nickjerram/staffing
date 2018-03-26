package org.camra.staffing.widgets.admin.layouts;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.ComponentContainer;

@SpringComponent
@UIScope
public class ScreenLayoutLogic extends ScreenLayout {

    public ComponentContainer getViewContainer() {
        return viewContainer;
    }

    public ComponentContainer getMenuContainer() {
        return menuContainer;
    }

}
