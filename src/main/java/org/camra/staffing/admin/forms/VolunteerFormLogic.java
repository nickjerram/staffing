package org.camra.staffing.admin.forms;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import org.camra.staffing.admin.grids.AreaSelectorGrid;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@UIScope
@SpringComponent
public class VolunteerFormLogic extends VolunteerForm {

    @Autowired private AreaSelectorGrid areaSelectorGrid;
    @Autowired private VolunteerService volunteerService;

    private BeanValidationBinder<VolunteerDTO> binder = new BeanValidationBinder<>(VolunteerDTO.class);
    private VolunteerDTO volunteer;

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
        save.addClickListener(event -> save());
        areaSelectorGrid.addItemClickListener(this::setAreaPreference);
    }

    public void editVolunteer(VolunteerDTO volunteerDTO) {
        this.volunteer = volunteerDTO;
        List<AreaSelectorDTO> areaList = volunteerService.getAreaSelectors(Optional.ofNullable(volunteerDTO.getId()));
        volunteerDTO.setAreas(areaList);
        binder.readBean(volunteerDTO);
        areaSelectorGrid.setSizeFull();
        areaSelectorGrid.setItems(areaList);
        formLayout.addComponent(areaSelectorGrid, 5);
        formLayout.setExpandRatio(areaSelectorGrid, 1f);
        show();
    }

    private void show() {
        addStyleName("visible");
    }

    private void hide() {
        removeStyleName("visible");
    }

    private void setAreaPreference(Grid.ItemClick<AreaSelectorDTO> select) {
        Grid.Column<AreaSelectorDTO,?> column = select.getColumn();
        if (column.getId()!=null) {
            AreaSelectorDTO area = select.getItem();
            Preference newPreference = Preference.valueOf(column.getId());
            Preference oldPreference = area.getPreference();
            area.setPreference(newPreference);
            volunteer.addArea(area);
            areaSelectorGrid.getDataProvider().refreshAll();
            save.setEnabled(newPreference!=oldPreference);
        }
    }

    private void save() {
        binder.writeBeanIfValid(volunteer);
        volunteerService.saveVolunteer(volunteer);
        hide();
    }

}
