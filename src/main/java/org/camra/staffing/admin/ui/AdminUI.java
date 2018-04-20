package org.camra.staffing.admin.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.admin.views.MainAssignmentView;
import org.camra.staffing.admin.views.SessionView;
import org.camra.staffing.admin.views.VolunteerView;
import org.camra.staffing.controller.FormManager;
import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.data.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.camra.staffing.admin.layouts.ScreenLayoutLogic;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@SpringUI(path="/admin")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class AdminUI extends UI implements ViewChangeListener {

    @Autowired private ScreenLayoutLogic mainLayout;
    @Autowired private VolunteerView volunteerView;
    @Autowired private MainAssignmentView mainAssignmentView;
    @Autowired private SessionView sessionView;
    @Lazy @Autowired private MenuLayoutLogic menu;
    @Autowired private Manager manager;

    protected void init(VaadinRequest request) {
        setLocale(request.getLocale());
        getPage().setTitle("Staffing Administration");

        if (manager.getUser().isPresent()) {
            showMainLayout();
        } else {
            setContent(new Label("Access Denied"));
        }
    }

    private void showMainLayout() {
        new Navigator(this, mainLayout.getViewContainer());
        addStyleName(ValoTheme.UI_WITH_MENU);
        menu.addVolunteerMenuItem(VaadinIcons.GROUP, volunteerView, true);
        menu.addVolunteerMenuItem(VaadinIcons.CALENDAR_USER, mainAssignmentView, false);
        menu.addSessionMenuItem(VaadinIcons.CALENDAR_CLOCK, sessionView);
        mainLayout.getMenuContainer().addComponent(menu);
        setContent(mainLayout);
        getNavigator().addViewChangeListener(this);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        return true;
    }

    public void update(Object o) {
        Notification.show("Update : "+o);
    }

}
