package org.camra.staffing.ui.volunteer.forms;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button;
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
public class EmailForm extends VerticalLayout {
    protected FormLayout emailForm;
    protected Label errors;
    protected TextField forename;
    protected TextField surname;
    protected TextField email;
    protected TextField confirmEmail;
    protected CssLayout captchaContainer;
    protected TextField captchaVerify;
    protected Button submit;

    public EmailForm() {
        Design.read(this);
    }
}