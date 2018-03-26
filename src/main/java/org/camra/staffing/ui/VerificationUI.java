package org.camra.staffing.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import org.camra.staffing.widgets.authentication.VerificationLoginLayoutLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@SpringUI(path="/")
@Theme("mytheme")
@Viewport("user-scalable=no,initial-scale=1.0")
public class VerificationUI extends StaffingUI {

    @Lazy @Autowired private VerificationLoginLayoutLogic verification;

    @Override
    protected Component content() {
        return verification;
    }

}
