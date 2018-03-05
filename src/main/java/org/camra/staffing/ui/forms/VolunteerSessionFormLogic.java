package org.camra.staffing.ui.forms;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.AssignmentSelectorDTO;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.ui.grids.Columns;

import java.util.List;

public class VolunteerSessionFormLogic extends VolunteerSessionForm {

    VolunteerSessionFormLogic(VolunteerSessionDTO volunteerSession, List<AssignmentSelectorDTO> possibleAssignments) {
        Grid<AssignmentSelectorDTO> grid = new Grid<>();
        grid.addColumn(this::formatArea, new HtmlRenderer()).setCaption("Area");
        grid.addColumn(this::formatRatio, new HtmlRenderer()).setCaption("Assigned/Required");
        grid.setSizeFull();
        grid.setItems(possibleAssignments);
        formLayout.addComponent(grid, 1);
        formLayout.getExpandRatio(grid);
    }


    private String formatRatio(AssignmentSelectorDTO selection) {
        return Columns.formatRatio(selection.getStaffAssigned(), selection.getStaffRequired());
    }

    private String formatArea(AssignmentSelectorDTO selection) {
        String preference = Columns.getIconCode("#ff0",selection.getAreaPreference()== Preference.Yes ? VaadinIcons.STAR : null);
        String current = Columns.getIconCode("#0a0",selection.isCurrent() ? VaadinIcons.CHECK_SQUARE : null);
        return preference+current+" "+selection.getAreaName();
    }

}
