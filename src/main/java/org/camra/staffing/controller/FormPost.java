package org.camra.staffing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.admin.access.AdminLoginService;
import org.camra.staffing.data.service.CamraAuthentication;
import org.camra.staffing.data.service.FormService;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.email.EmailMessage;
import org.camra.staffing.email.EmailSender;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;
import java.util.UUID;

@Controller
@SessionScope
public class FormPost {

    @Autowired private FormManager formManager;
    @Autowired private CamraAuthentication camraAuthentication;
    @Autowired private VolunteerService volunteerService;
    @Autowired private FormService formService;
    @Autowired private EmailSender emailSender;
    @Autowired private AdminLoginService loginService;

    @Value("${verification.failure}") private String failureMessage;
    @Value("${verification.success}") private String successMessage;
    @Value("${mainform.retrieved}") private String retrieved;
    @Value("${validation.captcha}") private String captchaMessage;
    @Value("${email.welcome.subject}") private String emailSubject;
    @Value("${email.welcome}") private String emailBody;
    @Value("${email.message}") private String emailSent;
    @Value("${message.completed}") private String completed;



    @PostMapping(path = "/form/submit", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public FormDTO submitForm(@RequestBody FormDTO requestData) throws JsonProcessingException {

        switch (formManager.getState()) {
            case Verify: return submitVerification(requestData);
            case Email: return submitEmailForm(requestData);
            case VolunteerFound:
            case Form: return submitMainForm(requestData);
            default: return errorCondition();
        }

    }

    private FormDTO submitVerification(FormDTO dto) {

        Optional<CamraMember> member = camraAuthentication.requestMemberDetails(dto.membership, dto.password);
        Optional<VolunteerDTO> volunteer = volunteerService.getVolunteer(member);
        FormDTO result;

        if (member.isPresent()) {
            result = formService.populateDTO(volunteer);
            formService.populateMember(member.get(), result);
            if (volunteer.isPresent()) {
                result.response.messages.add(retrieved);
            } else {
                result.response.messages.add(successMessage);
            }
            result.response.success = true;
            result.mainForm = true;
            result.submit = true;
            result.verified = true;
            formManager.setForm();
        } else {
            result = new FormDTO();
            result.response.error = true;
            result.response.errors.add(failureMessage);
            result.verificationForm = true;
            result.submit = true;
        }
        return result;
    }

    private FormDTO submitEmailForm(FormDTO dto) {
        FormDTO result = new FormDTO();

        if (!formManager.verifyCaptcha(dto.captcha)) {
            result.response.errors.add(captchaMessage);
            result.submit = true;
        }

        result.response.success = result.response.errors.isEmpty();
        result.response.error = !result.response.success;
        result.emailForm = true;
        result.forename = dto.forename;
        result.surname = dto.surname;
        result.email = dto.email;
        result.confirmEmail = dto.confirmEmail;
        result.captcha = dto.captcha;

        if (result.response.success) {
            result.response.messages.add(emailSent.replace("EMAIL", dto.email));
            result.emailForm = false;
            formManager.setFinished();
            VolunteerDTO volunteer = formService.populateDTO(dto, Optional.empty());
            volunteer.setAreas(volunteerService.getAreaSelectors(Optional.of(0)));
            volunteer.setUuid(UUID.randomUUID().toString());
            sendEmail(volunteer);
            volunteerService.saveVolunteer(volunteer);
        }
        return result;
    }

    private FormDTO submitMainForm(FormDTO requestData) {
        Optional<VolunteerDTO> existing = volunteerService.getVolunteer(requestData.id);
        VolunteerDTO volunteer = formService.populateDTO(requestData, existing);

        volunteerService.saveVolunteer(volunteer);
        FormDTO response = new FormDTO();
        response.response.messages.add(completed);
        response.response.success = true;
        return response;
    }

    private FormDTO errorCondition() {
        FormDTO response = new FormDTO();
        response.response.errors.add("404");
        response.response.error = true;
        return response;
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
