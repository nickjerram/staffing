package org.camra.staffing.ui.layouts;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.ui.grids.AreaSelectorGrid;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@UIScope
@SpringComponent
public class VolunteerFormLogic extends VolunteerForm {

    @Autowired private AreaSelectorGrid areaSelectorGrid;
    private BeanValidationBinder<VolunteerDTO> binder = new BeanValidationBinder<>(VolunteerDTO.class);

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        binder.forField(forename).bind("forename");
        binder.forField(surname).bind("surname");
        binder.forField(email).bind("email");
        binder.forField(role).bind("role");
        binder.forField(membership).bind("membership");
        binder.forField(firstaid).bind("firstaid");
        binder.forField(sia).bind("sia");
        binder.forField(cellar).bind("cellar");
        binder.addStatusChangeListener(event -> save.setEnabled(!event.hasValidationErrors() && binder.hasChanges()));
        cancel.addClickListener(event -> hide());
    }

    public void editVolunteer(VolunteerDTO volunteerDTO) {
        binder.readBean(volunteerDTO);
        areaSelectorGrid.setSizeFull();
        areaSelectorGrid.setVolunteerId(volunteerDTO.getId());
        areaSelectorHolder.addComponent(areaSelectorGrid);
        show();
    }

    public void show() {
        addStyleName("visible");
    }

    public void hide() {
        removeStyleName("visible");
    }
}
