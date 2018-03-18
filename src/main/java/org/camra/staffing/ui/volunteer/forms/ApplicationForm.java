package org.camra.staffing.ui.volunteer.forms;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * !! DO NOT EDIT THIS FILE !!
 * <p>
 * This class is generated by Vaadin Designer and will be overwritten.
 * <p>
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements View { }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class ApplicationForm extends CssLayout {
    protected TextField forename;
    protected TextField membership;
    protected TextField surname;
    protected TextField email;
    protected TextField enterMembership;
    protected TextField manager;
    protected CheckBox sia;
    protected CheckBox forklift;
    protected CheckBox firstaid;
    protected CheckBox cellar;
    protected GridLayout areas;
    protected Label level0;
    protected Label level1;
    protected Label level2;
    protected Label level3;
    protected Label level4;
    protected Label level5;
    protected Label level6;
    protected Label level7;
    protected Label level8;
    protected VerticalLayout sessions;
    protected Label title;
    protected Label sideLabel;

    public ApplicationForm() {
        Design.read(this);
    }
}
