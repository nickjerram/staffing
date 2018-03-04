package org.camra.staffing;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ui.layouts.MainLayoutLogic;
import org.camra.staffing.ui.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.views.SessionView;
import org.camra.staffing.ui.views.VolunteerView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@SpringUI
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class AdminUI extends UI implements ViewChangeListener {

    @Autowired private MainLayoutLogic mainLayout;
    @Autowired private VolunteerView volunteerView;
    @Lazy @Autowired private MenuLayoutLogic menu;

    protected void init(VaadinRequest request) {
        new Navigator(this, mainLayout.getViewContainer());
        setLocale(request.getLocale());
        getPage().setTitle("Volunteering");

        showMainLayout();
    }

    private void showMainLayout() {
        addStyleName(ValoTheme.UI_WITH_MENU);
        menu.addVolunteerMenuItem(VaadinIcons.GROUP, volunteerView, true);
        menu.addSessionMenuItem(VaadinIcons.CALENDAR_CLOCK, new SessionView());
        mainLayout.getMenuContainer().addComponent(menu);
        setContent(mainLayout);
        getNavigator().addViewChangeListener(this);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        return true;
    }
}
