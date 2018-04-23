package org.camra.staffing.admin.views;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Label;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.forms.VolunteerSessionFormLogic;
import org.camra.staffing.admin.grids.VolunteerSessionGrid;
import org.camra.staffing.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.admin.layouts.ViewLayout;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class VolunteerSessionView extends ViewLayout implements StaffingView {

    @Autowired private VolunteerService volunteerService;
    @Autowired private ApplicationContext context;
    @Autowired private VolunteerSessionGrid grid;
    @Autowired private Manager manager;
    private VolunteerSessionFormLogic form;
    private String name;
    @Lazy @Autowired private MenuLayoutLogic menu;


    void setVolunteer(VolunteerDTO volunteer) {
        this.form = context.getBean(VolunteerSessionFormLogic.class);
        this.name = volunteer.getForename()+" "+volunteer.getSurname();
        this.title.setValue("Sessions for "+name);
        if (manager.isSuperUser()) {
            this.extraHolder.addComponent(new Label(volunteer.getComment()));
        }
        this.newButton.setVisible(manager.isSuperUser());
        this.newButton.setCaption("Edit Sessions");
        this.newButton.addClickListener(event-> form.editSessions(volunteer));
        this.closeButton.setCaption("Close");
        this.closeButton.addClickListener(event -> menu.removeVolunteerMenuItem(this));

        grid.setVolunteerId(volunteer.getId());
        grid.setEditHandler(this::showReassignmentForm);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        form.setSaveHandler(vs -> grid.getDataProvider().refreshAll());
    }

    void setSession(SessionSelectorDTO session) {
        this.form = context.getBean(VolunteerSessionFormLogic.class);
        this.name = session.getSessionName();
        this.title.setValue("Assignments for "+session.getDescription());
        this.newButton.setVisible(false);
        this.closeButton.setCaption("Close");
        this.closeButton.addClickListener(event -> menu.removeSessionMenuItem(this));

        grid.setSessionId(session.getSessionId());
        grid.setEditHandler(this::showReassignmentForm);
        gridHolder.addComponent(grid);
        formHolder.addComponent(form);
        form.setSaveHandler(vs -> grid.getDataProvider().refreshAll());
    }

    private void showReassignmentForm(VolunteerSessionDTO volunteerSessionDTO) {
        form.reassign(volunteerSessionDTO, volunteerService.getPossibleReassignments(volunteerSessionDTO));
    }


    public String getName() {
        return name;
    }
}
