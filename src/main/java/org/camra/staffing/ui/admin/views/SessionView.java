package org.camra.staffing.ui.admin.views;

import com.vaadin.ui.Grid;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.ui.admin.layouts.ViewLayout;

public class SessionView extends ViewLayout implements StaffingView {

    public SessionView() {
        title.setValue("Session View");
        newButton.setVisible(false);
        closeButton.setVisible(false);



    }

    @Override
    public String getName() {
        return "Sessions";
    }
}
