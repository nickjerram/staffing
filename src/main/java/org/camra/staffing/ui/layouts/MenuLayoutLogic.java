package org.camra.staffing.ui.layouts;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.AdminUI;
import org.camra.staffing.ui.views.SessionView;
import org.camra.staffing.ui.views.StaffingView;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
@UIScope
public class MenuLayoutLogic extends MenuLayout {

    @Autowired private AdminUI adminUI;

    public MenuLayoutLogic() {
        MenuBar logoutMenu = new MenuBar();
        logoutMenu.addItem("Logout", VaadinIcons.SIGN_OUT, this::menuSelected);
        logoutMenu.addStyleName("user-menu");
        menuPart.addComponent(logoutMenu, 1);
    }

    public void addVolunteerMenuItem(Resource icon, StaffingView view) {
        addVolunteerMenuItem(icon, view, false);
    }

    public void addVolunteerMenuItem(Resource icon, StaffingView view, boolean defaultView) {
        volunteerMenuItems.addComponent(createButton(icon, view.getName()));

        adminUI.getNavigator().addView(view.getName(), view);
        if (defaultView) {
            adminUI.getNavigator().addView("", view);
        }
    }

    public void addSessionMenuItem(Resource icon, SessionView view) {
        sessionMenuItems.addComponent(createButton(icon, view.getName()));
        adminUI.getNavigator().addView(view.getName(), view);
    }

    private Button createButton(Resource icon, String caption) {
        Button button = new Button(caption);
        button.setIcon(icon);
        button.setPrimaryStyleName(ValoTheme.MENU_ITEM);
        button.addClickListener(event -> adminUI.getNavigator().navigateTo(caption));
        return button;
    }

    private void menuSelected(MenuBar.MenuItem selectedItem) {

    }

}
