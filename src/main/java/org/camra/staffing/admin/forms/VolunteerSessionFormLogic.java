package org.camra.staffing.admin.forms;

import com.vaadin.data.*;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.spring.annotation.SpringComponent;
import org.camra.staffing.admin.grids.ReassignmentGrid;
import org.camra.staffing.admin.grids.SessionSelectorGrid;
import org.camra.staffing.admin.ui.AdminUI;
import org.camra.staffing.admin.ui.TimeConverter;
import org.camra.staffing.data.dto.AssignmentSelectorDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class VolunteerSessionFormLogic extends VolunteerSessionForm {

    private enum State {AREAS, SESSIONS}

    @Autowired private VolunteerService volunteerService;
    @Autowired private ApplicationContext context;
    private ReassignmentGrid reassignmentGrid;
    private SessionSelectorGrid sessionSelectorGrid;
    private AssignmentSelectorDTO newAssignment;
    private VolunteerSessionDTO volunteerSession;
    private VolunteerDTO volunteer;
    private BeanValidationBinder<VolunteerSessionDTO> binder = new BeanValidationBinder<>(VolunteerSessionDTO.class);
    private Optional<Consumer<VolunteerSessionDTO>> saveHandler = Optional.empty();
    private State state;


    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        binder.forField(worked).bind(VolunteerSessionDTO::isWorked, VolunteerSessionDTO::setWorked);
        binder.forField(tokens).withConverter(new StringToIntegerConverter(("Must be an integer")))
                .bind(VolunteerSessionDTO::getTokens, VolunteerSessionDTO::setTokens);
        binder.forField(comment).bind(VolunteerSessionDTO::getComment, VolunteerSessionDTO::setComment);
        binder.forField(locked).bind(VolunteerSessionDTO::isLocked, VolunteerSessionDTO::setLocked);
        binder.forField(start).withConverter(new TimeConverter())
                .bind(VolunteerSessionDTO::getStart, VolunteerSessionDTO::setStartTime);
        binder.forField(finish).withConverter(new TimeConverter())
                .bind(VolunteerSessionDTO::getFinish, VolunteerSessionDTO::setFinishTime);
        binder.addStatusChangeListener(event -> save.setEnabled(!event.hasValidationErrors() && binder.hasChanges()));
        cancel.addClickListener(event -> hide());
        save.setEnabled(false);
        save.addClickListener(event -> save());
    }

    public void editSessions(VolunteerDTO volunteer) {
        this.state = State.SESSIONS;
        this.volunteer = volunteer;
        title.setValue(volunteer.getForename()+" "+volunteer.getSurname());
        subtitle.setValue("New Session");
        sessionSelectorGrid = context.getBean(SessionSelectorGrid.class);
        sessionSelectorGrid.setVisible(true);
        sessionSelectorGrid.setVolunteerId(volunteer.getId());
        sessionSelectorGrid.setChangeHandler(this::sessionsChanged);
        sessionSelectorGrid.setSizeFull();
        formLayout.addComponent(sessionSelectorGrid, 4);
        formLayout.setExpandRatio(sessionSelectorGrid, 1);
        addStyleName("visible");
    }

    private void sessionsChanged(Void aVoid) {
        save.setEnabled(true);
    }


    public void reassign(VolunteerSessionDTO volunteerSession, List<AssignmentSelectorDTO> options) {
        this.state = State.AREAS;
        this.volunteerSession = volunteerSession;
        binder.readBean(volunteerSession);
        title.setValue(volunteerSession.getForename()+" "+volunteerSession.getSurname());
        subtitle.setValue(volunteerSession.getSessionName());
        reassignmentGrid = context.getBean(ReassignmentGrid.class);
        reassignmentGrid.setVisible(true);
        reassignmentGrid.setSizeFull();
        reassignmentGrid.setItems(options);
        reassignmentGrid.setReassignmentHandler(this::reassign);
        reassignmentGrid.setSizeFull();
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
        switch (state) {
            case AREAS:
                tryWriteBean();
                if (newAssignment!=null) {
                    volunteerSession.setAreaId(newAssignment.getAreaId());
                }
                volunteerService.saveAssignment(volunteerSession);
                break;
            case SESSIONS:
                List<Integer> toAdd = sessionSelectorGrid.getSessionsToAdd().stream().map(SessionSelectorDTO::getSessionId).collect(Collectors.toList());
                volunteerService.saveVolunteerSession(volunteer.getId(), toAdd);
                List<Integer> toRemove = sessionSelectorGrid.getSessionsToRemove().stream().map(SessionSelectorDTO::getSessionId).collect(Collectors.toList());
                volunteerService.removeVolunteerSession(volunteer.getId(), toRemove);
        }

        hide();
        saveHandler.ifPresent(h -> h.accept(volunteerSession));
    }

    private void tryWriteBean() {
        try {
            binder.writeBean(volunteerSession);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private void hide() {
        if (reassignmentGrid!=null) {
            formLayout.removeComponent(reassignmentGrid);
            reassignmentGrid = null;
        }
        if (sessionSelectorGrid!=null) {
            formLayout.removeComponent(sessionSelectorGrid);
            sessionSelectorGrid = null;
        }
        removeStyleName("visible");
    }

}
