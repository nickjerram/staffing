package org.camra.staffing.controller;

import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Optional;

@Controller
@SessionScope
public class JSONController {

    @Autowired private FormManager formManager;
    @Autowired private FormService formService;
    @Value("${mainform.retrieved}") private String retrieved;
    @Value("${mainform.notfound}") private String notFound;


    @GetMapping("/form/json")
    @ResponseBody
    public FormDTO getForm() {

        switch (formManager.getState()) {
            case Verify: return getVerify();
            case VolunteerFound: return getVolunteerFound();
            case Email: return getEmailForm();
            default: return getVolunteerNotFound();
        }

    }

    private FormDTO getVerify() {
        FormDTO dto = new FormDTO();
        dto.verificationForm = true;
        dto.submit = true;
        return dto;
    }

    private FormDTO getVolunteerFound() {
        VolunteerDTO volunteer = formManager.getVolunteer();
        FormDTO dto = formService.populateDTO(Optional.of(volunteer));
        dto.mainForm = true;
        dto.response.messages.add(retrieved);
        dto.response.success = true;
        dto.submit = true;
        return dto;
    }

    private FormDTO getVolunteerNotFound() {
        FormDTO dto = new FormDTO();
        dto.response.errors.add(notFound);
        dto.response.error = true;
        return dto;
    }

    private FormDTO getEmailForm() {
        FormDTO dto = new FormDTO();
        dto.emailForm = true;
        dto.submit = true;
        return dto;
    }

    @GetMapping("/form/include/{path}")
    public String getInclude(@PathVariable String path) {
        return path;
    }


}
