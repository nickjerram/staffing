package org.camra.staffing.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.camra.staffing.widgets.admin.layouts.ScreenLayoutLogic;
import org.camra.staffing.widgets.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.widgets.admin.views.SessionView;
import org.camra.staffing.widgets.admin.views.VolunteerView;
import org.camra.staffing.widgets.authentication.AdminLoginLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.Optional;

@SpringUI(path="/admin")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class AdminUI extends UI implements ViewChangeListener {

    @Value("${admin.title}") private String adminTitle;
    @Value("${admin.message}") private String adminMessage;
    @Autowired private ScreenLayoutLogic mainLayout;
    @Autowired private VolunteerView volunteerView;
    @Lazy @Autowired private MenuLayoutLogic menu;
    @Lazy @Autowired private AdminLoginLayoutLogic adminLogin;
    @Autowired private Environment environment;
    @Autowired private WelcomeLayoutLogic welcomeLayout;
    private Optional<AdminUser> currentUser = Optional.empty();

    protected void init(VaadinRequest request) {
        setLocale(request.getLocale());
        getPage().setTitle("Staffing Administration");

        if (environment.acceptsProfiles("dev") || currentUser.isPresent()) {
            showMainLayout();
        } else {
            welcomeLayout.setSideTitle(adminTitle);
            welcomeLayout.setSideText(adminMessage);
            welcomeLayout.setContent(adminLogin);
            setContent(welcomeLayout);
        }
    }

    public void setAdminUser(AdminUser adminUser) {
        this.currentUser = Optional.of(adminUser);
        showMainLayout();
    }

    private void showMainLayout() {
        new Navigator(this, mainLayout.getViewContainer());
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

    public void update(Object o) {
        Notification.show("Update : "+o);
    }
}
