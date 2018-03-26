package org.camra.staffing.widgets.admin.views;

import org.camra.staffing.widgets.admin.layouts.ViewLayout;

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
