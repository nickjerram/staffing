package org.camra.staffing.ui.views;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.ui.grids.VolunteerGrid;
import org.camra.staffing.ui.layouts.ViewLayout;
import org.camra.staffing.ui.layouts.VolunteerFormLogic;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class VolunteerView extends ViewLayout implements StaffingView {

    @Autowired private VolunteerFormLogic form;
    @Autowired private VolunteerGrid grid;

    @PostConstruct
    @SuppressWarnings("unused")
    void init() {

        title.setValue("Volunteer View");
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        newButton.setCaption("New Volunteer");
        newButton.addClickListener(event -> form.editVolunteer(new VolunteerDTO()));
    }

    @Override
    public String getName() {
        return "Volunteers";
    }

}
