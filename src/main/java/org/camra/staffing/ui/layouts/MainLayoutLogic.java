package org.camra.staffing.ui.layouts;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.ComponentContainer;
import org.camra.staffing.ui.layouts.MainLayout;

@SpringComponent
@UIScope
public class MainLayoutLogic extends MainLayout {

    public ComponentContainer getViewContainer() {
        return viewContainer;
    }

    public ComponentContainer getMenuContainer() {
        return menuContainer;
    }

}
