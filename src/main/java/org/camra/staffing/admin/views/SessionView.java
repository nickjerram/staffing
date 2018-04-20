package org.camra.staffing.admin.views;


import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.admin.grids.SessionGrid;
import org.camra.staffing.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.admin.layouts.ViewLayout;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class SessionView extends ViewLayout implements StaffingView {

    @Autowired private SessionGrid grid;
    @Lazy @Autowired private MenuLayoutLogic menu;
    @Autowired private ApplicationContext context;

    @PostConstruct
    private void init() {
        title.setValue("Session View");
        newButton.setVisible(false);
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);

        grid.setDetailViewHandler(session ->
            menu.addSessionMenuItem(VaadinIcons.CLOCK, createVolunteerSessionView(session))
        );


    }

    public VolunteerSessionView createVolunteerSessionView(SessionSelectorDTO session) {
        VolunteerSessionView view = context.getBean(VolunteerSessionView.class);
        view.setSession(session);
        return view;
    }

    @Override
    public String getName() {
        return "Sessions";
    }
}
