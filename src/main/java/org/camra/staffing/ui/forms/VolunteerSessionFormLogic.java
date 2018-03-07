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
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@UIScope
@SpringComponent
public class VolunteerSessionFormLogic extends VolunteerSessionForm {

    @Autowired private AdminUI ui;
    @Autowired private ReassignmentGrid reassignmentGrid;
    @Autowired private VolunteerService volunteerService;
    private AssignmentSelectorDTO newAssignment;
    private VolunteerSessionDTO volunteerSession;
    private Binder<VolunteerSessionDTO> binder = new Binder<>(VolunteerSessionDTO.class);
    private Optional<Consumer<VolunteerSessionDTO>> saveHandler = Optional.empty();

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        binder.forField(worked).bind(VolunteerSessionDTO::isWorked, VolunteerSessionDTO::setWorked);
        binder.forField(tokens).withConverter(new StringToIntegerConverter(("Must be an integer")))
                .bind(VolunteerSessionDTO::getTokens, VolunteerSessionDTO::setTokens);
        binder.forField(comment).bind(VolunteerSessionDTO::getComment, VolunteerSessionDTO::setComment);
        binder.forField(locked).bind(VolunteerSessionDTO::isLock, VolunteerSessionDTO::setLock);
        binder.addStatusChangeListener(event -> save.setEnabled(!event.hasValidationErrors() && binder.hasChanges()));
        cancel.addClickListener(event -> removeStyleName("visible"));
        save.addClickListener(event -> save());
    }


    public void reassign(VolunteerSessionDTO volunteerSession, List<AssignmentSelectorDTO> options) {
        this.volunteerSession = volunteerSession;
        binder.readBean(volunteerSession);
        title.setValue(volunteerSession.getForename()+" "+volunteerSession.getSurname());
        subtitle.setValue(volunteerSession.getSessionName());
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
        binder.writeBeanIfValid(volunteerSession);
        volunteerSession.setAreaId(newAssignment.getAreaId());
        volunteerService.saveAssignment(volunteerSession);
        ui.update(volunteerSession);
        removeStyleName("visible");
        saveHandler.ifPresent(h -> h.accept(volunteerSession));
    }


}
