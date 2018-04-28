package org.camra.staffing.admin.views;

import com.vaadin.data.HasValue;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CheckBox;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.grids.MainAssignmentGrid;
import org.camra.staffing.admin.layouts.ViewLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class MainAssignmentView extends ViewLayout implements StaffingView {

    @Autowired private MainAssignmentGrid grid;
    @Autowired private Manager manager;
    private CheckBox enableUnlocking = new CheckBox("Enable Unlocking");

    @Override
    public String getName() {
        return "Everything";
    }

    @PostConstruct
    private void init() {
        title.setValue("Main View");
        closeButton.setVisible(false);
        gridHolder.addComponent(grid);
        newButton.setVisible(false);

        if (manager.isSuperUser()) {
            enableUnlocking.addValueChangeListener(this::enableUnlocking);
            extraHolder.addComponent(enableUnlocking);
        }

    }

    private void enableUnlocking(HasValue.ValueChangeEvent<Boolean> event) {
        grid.setUnlockingEnabled(event.getValue());
    }


}
