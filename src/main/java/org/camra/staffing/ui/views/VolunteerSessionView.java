package org.camra.staffing.ui.views;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.spring.annotation.SpringComponent;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.forms.VolunteerSessionFormLogic;
import org.camra.staffing.ui.grids.VolunteerSessionGrid;
import org.camra.staffing.ui.layouts.ViewLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.stream.Stream;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class VolunteerSessionView extends ViewLayout implements StaffingView {

    @Autowired private VolunteerService volunteerService;
    @Autowired private VolunteerSessionFormLogic form;
    private String name;
    private VolunteerDTO volunteer;

    void setVolunteer(VolunteerDTO volunteer) {
        this.volunteer = volunteer;
        this.name = volunteer.getForename()+" "+volunteer.getSurname();
        this.title.setValue("Sessions for "+name);
        VolunteerSessionGrid grid = new VolunteerSessionGrid(VolunteerSessionGrid.Type.VOLUNTEER);
        grid.setDataProvider(DataProvider.fromCallbacks(this::getItems, this::countItems));
        grid.setEditHandler(this::showReassignmentForm);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        form.setSaveHandler(vs -> grid.getDataProvider().refreshAll());
    }

    private int countItems(Query<VolunteerSessionDTO, Void> query) {
        return volunteerService.countSessions(volunteer.getId());
    }

    private Stream<VolunteerSessionDTO> getItems(Query<VolunteerSessionDTO, Void> query) {
        return volunteerService.getSessions(volunteer.getId(), query).stream();
    }

    private void showReassignmentForm(VolunteerSessionDTO volunteerSessionDTO) {
        form.reassign(volunteerSessionDTO, volunteerService.getPossibleReassignments(volunteerSessionDTO));
    }


    public String getName() {
        return name;
    }
}
