package org.camra.staffing.ui.views;

import com.vaadin.ui.Grid;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.entity.VolunteerSession;
import org.camra.staffing.ui.layouts.ViewLayout;

public class VolunteerSessionView extends ViewLayout implements StaffingView {

    private VolunteerDTO volunteer;
    private Grid<VolunteerSessionDTO> grid;

    VolunteerSessionView(VolunteerDTO volunteer, Grid<VolunteerSessionDTO> grid) {
        this.volunteer = volunteer;
        this.grid = grid;
        title.setValue("Volunteer View");
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);
        //formHolder.addComponent(form);
        newButton.setCaption("New Session");
        //newButton.addClickListener(event -> form.editVolunteer(new VolunteerDTO()));
        //grid.setEditHandler(volunteer -> form.editVolunteer(volunteer));
    }

    VolunteerSessionView(SessionDTO session, Grid<VolunteerSessionDTO> grid) {

    }

    public String getName() {
        return volunteer.getForename()+" "+volunteer.getSurname();
    }
}
