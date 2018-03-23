package org.camra.staffing.ui.admin.views;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.ui.admin.layouts.ViewLayout;
import org.camra.staffing.ui.admin.grids.VolunteerGrid;
import org.camra.staffing.ui.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.admin.forms.VolunteerFormLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class VolunteerView extends ViewLayout implements StaffingView {

    @Autowired private VolunteerFormLogic form;
    @Autowired private VolunteerGrid grid;
    @Autowired private ApplicationContext context;
    @Lazy @Autowired private MenuLayoutLogic menu;

    public VolunteerSessionView createVolunteerSessionView(VolunteerDTO volunteer) {
        VolunteerSessionView view = context.getBean(VolunteerSessionView.class);
        view.setVolunteer(volunteer);
        return view;
    }

    @PostConstruct
    private void init() {

        title.setValue("Volunteer View");
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        newButton.setCaption("New Volunteer");
        newButton.addClickListener(event -> form.editVolunteer(new VolunteerDTO()));
        grid.setEditHandler(volunteer -> form.editVolunteer(volunteer));
        grid.setDetailViewHandler(volunteer ->
            menu.addVolunteerMenuItem(VaadinIcons.USER, createVolunteerSessionView(volunteer))
        );
    }

    @Override
    public String getName() {
        return "Volunteers";
    }

}
