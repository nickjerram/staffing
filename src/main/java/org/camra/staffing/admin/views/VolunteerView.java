package org.camra.staffing.admin.views;

import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.UI;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.forms.VolunteerFormLogic;
import org.camra.staffing.admin.grids.VolunteerGrid;
import org.camra.staffing.admin.layouts.MenuLayoutLogic;
import org.camra.staffing.admin.layouts.ViewLayout;
import org.camra.staffing.admin.popup.Confirmation;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.VolunteerService;
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
    @Autowired private VolunteerService volunteerService;
    @Autowired private Manager manager;
    @Lazy @Autowired private MenuLayoutLogic menu;
    private CheckBox enableConfirmations = new CheckBox("Enable Confirmation Emails");

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
        newButton.setVisible(manager.isSuperUser());
        newButton.setCaption("New Volunteer");
        newButton.addClickListener(event -> form.editVolunteer(new VolunteerDTO()));
        grid.setEditHandler(volunteer -> form.editVolunteer(volunteer));
        grid.setDetailViewHandler(volunteer ->
            menu.addVolunteerMenuItem(VaadinIcons.USER, createVolunteerSessionView(volunteer))
        );
        grid.setDeleteHandler(this::deleteVolunteer);

        if (manager.isSuperUser()) {
            enableConfirmations.addValueChangeListener(this::enableConfirmations);
            extraHolder.addComponent(enableConfirmations);
        }
    }

    private void enableConfirmations(HasValue.ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        grid.enableConfirmations(booleanValueChangeEvent.getValue());
    }

    private void deleteVolunteer(VolunteerDTO volunteerDTO) {
        String message = "Really delete "+volunteerDTO.getForename()+" "+volunteerDTO.getSurname()+"?";
        Confirmation confirmation = new Confirmation(message, ()-> {
            volunteerService.deleteVolunteer(volunteerDTO);
            grid.getDataProvider().refreshAll();
        });
        UI.getCurrent().addWindow(confirmation);
    }


    @Override
    public String getName() {
        return "Volunteers";
    }

}
