package org.camra.staffing.admin.layouts;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.ui.AdminUI;
import org.camra.staffing.admin.views.StaffingView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@SpringComponent
@UIScope
public class MenuLayoutLogic extends MenuLayout {

    @Autowired private AdminUI adminUI;
    @Autowired private Manager manager;

    private Map<String,Button> menuButtons = new HashMap<>();

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
        if (!menuButtons.containsKey(view.getName())) {
            Button button = createButton(icon, view.getName());
            menuButtons.put(view.getName(), button);
            volunteerMenuItems.addComponent(button);
            adminUI.getNavigator().addView(view.getName(), view);
            if (defaultView) {
                adminUI.getNavigator().addView("", view);
            }
        }
    }

    public void removeVolunteerMenuItem(StaffingView view) {
        Button buttonToRemove = menuButtons.remove(view.getName());
        volunteerMenuItems.removeComponent(buttonToRemove);
        adminUI.getNavigator().removeView(view.getName());
        adminUI.getNavigator().navigateTo("Volunteers");
    }

    public void addSessionMenuItem(Resource icon, StaffingView view) {
        if (!menuButtons.containsKey(view.getName())) {
            Button button = createButton(icon, view.getName());
            menuButtons.put(view.getName(), button);
            sessionMenuItems.addComponent(button);
            adminUI.getNavigator().addView(view.getName(), view);
        }
    }

    public void removeSessionMenuItem(StaffingView view) {
        Button buttonToRemove = menuButtons.remove(view.getName());
        buttonToRemove.setVisible(false);
        sessionMenuItems.removeComponent(buttonToRemove);
        adminUI.getNavigator().removeView(view.getName());
        adminUI.getNavigator().navigateTo("Sessions");
    }

    private Button createButton(Resource icon, String caption) {
        Button button = new Button(caption);
        button.setIcon(icon);
        button.setPrimaryStyleName(ValoTheme.MENU_ITEM);
        button.addClickListener(event -> adminUI.getNavigator().navigateTo(caption));
        return button;
    }

    private void menuSelected(MenuBar.MenuItem selectedItem) {
        manager.adminLogout();
        Page.getCurrent().open("/login",null);
    }

}
