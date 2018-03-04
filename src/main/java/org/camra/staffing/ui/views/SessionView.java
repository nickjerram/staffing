package org.camra.staffing.ui.views;

import com.vaadin.ui.Grid;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.ui.layouts.ViewLayout;

public class SessionView extends ViewLayout implements StaffingView {

    public SessionView() {
        title.setValue("Session View");
        newButton.setVisible(false);
        closeButton.setVisible(false);


        Grid<SessionDTO> sessionGrid = new Grid<>();
        sessionGrid.addColumn(SessionDTO::getName);
        sessionGrid.addColumn(SessionDTO::getStart);

        SessionDTO v1 = new SessionDTO();
        v1.setName("Monday");
        v1.setStart("09:00");
        SessionDTO v2 = new SessionDTO();
        v1.setName("Tuesday");
        v1.setStart("10:00");
        sessionGrid.setItems(v1,v2);

        gridHolder.addComponent(sessionGrid);

    }

    @Override
    public String getName() {
        return "Sessions";
    }
}
