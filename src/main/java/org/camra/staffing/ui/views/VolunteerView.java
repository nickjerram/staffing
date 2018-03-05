package org.camra.staffing.ui.views;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.ui.grids.VolunteerGrid;
import org.camra.staffing.ui.layouts.MenuLayoutLogic;
import org.camra.staffing.ui.layouts.ViewLayout;
import org.camra.staffing.ui.forms.VolunteerFormLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class VolunteerView extends ViewLayout implements StaffingView {

    @Autowired private VolunteerFormLogic form;
    @Autowired private VolunteerGrid grid;
    @Lazy @Autowired private MenuLayoutLogic menu;
    @Autowired private VolunteerSessionViewFactory volunteerSessionViewFactory;

    @PostConstruct
    @SuppressWarnings("unused")
    void init() {

        title.setValue("Volunteer View");
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        newButton.setCaption("New Volunteer");
        newButton.addClickListener(event -> form.editVolunteer(new VolunteerDTO()));
        grid.setEditHandler(volunteer -> form.editVolunteer(volunteer));
        grid.setVolunteerSessionsViewHandler(volunteer ->
            menu.addVolunteerMenuItem(VaadinIcons.USER, volunteerSessionViewFactory.createViewForVolunteer(volunteer))
        );
    }

    @Override
    public String getName() {
        return "Volunteers";
    }

}
