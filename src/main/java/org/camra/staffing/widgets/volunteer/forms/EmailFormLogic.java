package org.camra.staffing.widgets.volunteer.forms;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.server.ExternalResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import org.camra.staffing.ui.EmailUI;
import org.camra.staffing.data.dto.EmailDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.email.EmailMessage;
import org.camra.staffing.email.EmailSender;
import org.camra.staffing.util.CaptchaController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
public class EmailFormLogic extends EmailForm {

    @Autowired private CaptchaController captcha;
    @Autowired private EmailSender emailSender;
    @Autowired private VolunteerService volunteerService;
    @Autowired private EmailUI emailUI;
    @Value("${email.welcome}") private String emailBody;
    @Value("${email.welcome.subject}") private String emailSubject;
    @Value("${email.message}") private String emailMessageText;
    @Value("${emailform.title}") private String formTitle;
    @Value("${emailform.message}") private String formMessage;

    private BeanValidationBinder<EmailDTO> binder = new BeanValidationBinder<>(EmailDTO.class);
    private List<String> errorMessages = new ArrayList<>();

    @PostConstruct
    private void init() {
        title.setValue(formTitle);
        message.setValue(formMessage);
        binder.forField(forename).bind("forename");
        binder.forField(surname).bind("surname");
        binder.forField(email).bind("email");
        binder.forField(confirmEmail).bind("confirmation");
        binder.addStatusChangeListener(event-> submit.setEnabled(!event.hasValidationErrors() && binder.hasChanges()));
    }

    public void show() {
        Image image = new Image();
        image.setSource(new ExternalResource("/captcha"));
        captchaContainer.addComponent(image);
        submit.addClickListener(this::submit);
    }

    private void submit(Button.ClickEvent clickEvent) {
        EmailDTO email = new EmailDTO();
        binder.writeBeanIfValid(email);
        validate(email);

        if (errorMessages.isEmpty()) {
            VolunteerDTO volunteer = createVolunteer(email);
            sendEmail(volunteer);
            emailUI.showMessage("Thank You!",emailMessageText.replace("EMAIL", volunteer.getEmail()));
        }
    }

    private void validate(EmailDTO email) {
        errorMessages.clear();
        boolean captchaMatch = captcha.getWord().equalsIgnoreCase(captchaVerify.getValue().trim());
        if (!captchaMatch) {
            errorMessages.add("Re-check the word in the image");
        }
        if (!email.isEmailOK()) {
            errorMessages.add("Email Addresses do not match");
        }
        if (!email.isNameOK()) {
            errorMessages.add("Please provide your full name");
        }
        errors.setVisible(!errorMessages.isEmpty());
        errors.setValue(errorMessages.stream().collect(Collectors.joining("<br/>")));
    }

    private VolunteerDTO createVolunteer(EmailDTO email) {
        VolunteerDTO volunteer = new VolunteerDTO();
        volunteer.setUuid(UUID.randomUUID().toString());
        volunteer.setForename(email.getForename());
        volunteer.setSurname(email.getSurname());
        volunteer.setEmail(email.getEmail());
        volunteerService.saveVolunteer(volunteer);
        return volunteer;
    }

    private void sendEmail(VolunteerDTO volunteer) {
        String messageBody = emailBody
                .replace("UUID", volunteer.getUuid())
                .replace("FORENAME", volunteer.getForename())
                .replace("SURNAME", volunteer.getSurname());
        EmailMessage message = new EmailMessage();
        message.addRecipient(volunteer.getEmail());
        message.setBody(messageBody);
        message.setSubject(emailSubject);
        emailSender.sendMessage(message);
    }


}
