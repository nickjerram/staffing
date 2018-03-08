package org.camra.staffing.ui.forms;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import jdk.nashorn.internal.ir.Optimistic;
import org.camra.staffing.AdminUI;
import org.camra.staffing.data.dto.AssignmentSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.entity.VolunteerSession;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.grids.ReassignmentGrid;
import org.camra.staffing.ui.grids.SessionSelectorGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class VolunteerSessionFormLogic extends VolunteerSessionForm {

    @Autowired private AdminUI ui;
    @Autowired private VolunteerService volunteerService;
    @Autowired private ApplicationContext context;
    private ReassignmentGrid reassignmentGrid;
    private SessionSelectorGrid sessionSelectorGrid;
    private AssignmentSelectorDTO newAssignment;
    private VolunteerSessionDTO volunteerSession;
    private VolunteerDTO volunteer;
    private Binder<VolunteerSessionDTO> binder = new Binder<>(VolunteerSessionDTO.class);
    private Optional<Consumer<VolunteerSessionDTO>> saveHandler = Optional.empty();

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        binder.forField(worked).bind(VolunteerSessionDTO::isWorked, VolunteerSessionDTO::setWorked);
        binder.forField(tokens).withConverter(new StringToIntegerConverter(("Must be an integer")))
                .bind(VolunteerSessionDTO::getTokens, VolunteerSessionDTO::setTokens);
        binder.forField(comment).bind(VolunteerSessionDTO::getComment, VolunteerSessionDTO::setComment);
        binder.forField(locked).bind(VolunteerSessionDTO::isLocked, VolunteerSessionDTO::setLocked);
        binder.addStatusChangeListener(event -> save.setEnabled(!event.hasValidationErrors() && binder.hasChanges()));
        cancel.addClickListener(event -> removeStyleName("visible"));
        save.setEnabled(false);
        save.addClickListener(event -> save());
    }

    public void newSession(VolunteerDTO volunteer) {
        this.volunteer = volunteer;
        title.setValue(volunteer.getForename()+" "+volunteer.getSurname());
        subtitle.setValue("New Session");
        sessionSelectorGrid = context.getBean(SessionSelectorGrid.class);
        sessionSelectorGrid.setVisible(true);
        sessionSelectorGrid.setVolunteerId(volunteer.getId());
        sessionSelectorGrid.setChangeHandler(this::sessionsChanged);
        formLayout.addComponent(sessionSelectorGrid, 4);
        formLayout.setExpandRatio(sessionSelectorGrid, 1);
        addStyleName("visible");
    }

    private void sessionsChanged(Void aVoid) {
        save.setEnabled(true);
    }


    public void reassign(VolunteerSessionDTO volunteerSession, List<AssignmentSelectorDTO> options) {
        this.volunteerSession = volunteerSession;
        binder.readBean(volunteerSession);
        title.setValue(volunteerSession.getForename()+" "+volunteerSession.getSurname());
        subtitle.setValue(volunteerSession.getSessionName());
        reassignmentGrid = context.getBean(ReassignmentGrid.class);
        reassignmentGrid.setVisible(true);
        reassignmentGrid.setSizeFull();
        reassignmentGrid.setItems(options);
        reassignmentGrid.setReassignmentHandler(this::reassign);
        formLayout.addComponent(reassignmentGrid,4);
        formLayout.setExpandRatio(reassignmentGrid, 1);
        addStyleName("visible");
    }

    public void setSaveHandler(Consumer<VolunteerSessionDTO> handler) {
        this.saveHandler = Optional.of(handler);
    }

    private void reassign(AssignmentSelectorDTO assignmentSelectorDTO) {
        this.newAssignment = assignmentSelectorDTO;
        save.setEnabled(true);
    }

    private void save() {
        if (volunteerSession!=null) {
            binder.writeBeanIfValid(volunteerSession);
            volunteerSession.setAreaId(newAssignment.getAreaId());
            volunteerService.saveAssignment(volunteerSession);
        } else if (volunteer!=null) {
            volunteerService.saveVolunteerSession(volunteer.getId(), sessionSelectorGrid.getNewSessions());
        }
        removeStyleName("visible");
        saveHandler.ifPresent(h -> h.accept(volunteerSession));
    }


}
