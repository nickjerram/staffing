package org.camra.staffing;

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
import lombok.Setter;
import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.ui.admin.layouts.ScreenLayoutLogic;
import org.camra.staffing.ui.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.admin.views.SessionView;
import org.camra.staffing.ui.admin.views.VolunteerView;
import org.camra.staffing.ui.authentication.AdminLoginLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;

@SpringUI(path="/admin")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class AdminUI extends UI implements ViewChangeListener {

    private Optional<AdminUser> currentUser = Optional.empty();
    @Autowired private ScreenLayoutLogic mainLayout;
    @Autowired private VolunteerView volunteerView;
    @Lazy @Autowired private MenuLayoutLogic menu;
    @Lazy @Autowired private AdminLoginLayoutLogic adminLogin;

    protected void init(VaadinRequest request) {
        setLocale(request.getLocale());
        getPage().setTitle("Staffing Administration");

        if (currentUser.isPresent()) {
            showMainLayout();
        } else {
            setContent(adminLogin);
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
