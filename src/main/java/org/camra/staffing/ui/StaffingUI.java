package org.camra.staffing.ui;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.widgets.layouts.MobileLayoutLogic;
import org.camra.staffing.widgets.layouts.StaffingLayout;
import org.camra.staffing.widgets.layouts.WelcomeLayoutLogic;
import org.camra.staffing.widgets.volunteer.message.MessageViewLogic;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class StaffingUI extends UI {

    @Autowired private WelcomeLayoutLogic welcomeLayout;
    @Autowired private MobileLayoutLogic mobileLayout;
    @Autowired private MessageViewLogic messageView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        addStyleName(ValoTheme.UI_WITH_MENU);
        getPage().setTitle("Volunteering");
        chooseLayout().setContent(content());
        setContent(chooseLayout());
    }

    protected boolean isMobile() {
        return Page.getCurrent().getBrowserWindowWidth()<600;
    }

    protected StaffingLayout chooseLayout() {
        return isMobile() ? mobileLayout : welcomeLayout;
    }

    public void showMessage(String title, String message) {
        messageView.setMessage(message);
        messageView.setTitle(title);
        chooseLayout().setContent(messageView);
    }

    protected abstract Component content();
}
