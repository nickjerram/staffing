package org.camra.staffing.ui.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.AssignmentSelectorDTO;
import org.camra.staffing.data.entity.Preference;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class ReassignmentGrid extends Grid<AssignmentSelectorDTO> {

    private AssignmentSelectorDTO currentAssignment;
    private Consumer<AssignmentSelectorDTO> reassignmentHandler;

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        addColumn(this::formatArea, new HtmlRenderer()).setCaption("Area");
        addColumn(this::formatRatio, new HtmlRenderer()).setCaption("Assigned/Required");
        setSizeFull();
        addItemClickListener(this::reassign);
    }

    public void setReassignmentHandler(Consumer<AssignmentSelectorDTO> handler) {
        this.reassignmentHandler = handler;
    }

    public void setItems(List<AssignmentSelectorDTO> items) {
        items.forEach(item -> {
            if(item.isSelected()) {
                currentAssignment = item;
            }
        });
        super.setItems(items);
    }

    private void reassign(ItemClick<AssignmentSelectorDTO> itemClick) {
        AssignmentSelectorDTO newAssignment = itemClick.getItem();
        if (newAssignment.getAreaId()!=currentAssignment.getAreaId()) {
            currentAssignment.removeAssignment();
            newAssignment.addAssignment();
            currentAssignment = newAssignment;
            getDataProvider().refreshAll();
            if (reassignmentHandler!=null) {
                reassignmentHandler.accept(newAssignment);
            }
        }
    }

    private String formatRatio(AssignmentSelectorDTO selection) {
        return Columns.formatRatio(selection.getAssigned(), selection.getRequired());
    }

    private String formatArea(AssignmentSelectorDTO selection) {
        String preference = Columns.getIconCode("#ff0",selection.getPreference()== Preference.Yes ? VaadinIcons.STAR : null);
        String current = Columns.getIconCode("#0a0",selection.isSelected() ? VaadinIcons.CHECK_SQUARE : null);
        return preference+current+" "+selection.getName();
    }

}
