package org.camra.staffing.widgets.admin.views;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.widgets.admin.grids.MainAssignmentGrid;
import org.camra.staffing.widgets.admin.layouts.ViewLayout;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class MainAssignmentView extends ViewLayout implements StaffingView {

    @Autowired private MainAssignmentGrid grid;

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
    }


}
