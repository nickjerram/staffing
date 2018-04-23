package org.camra.staffing.email;

import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class ConfirmationMessageBuilder {

    @Autowired private TemplateEngine templateEngine;
    @Autowired private VolunteerService volunteerService;

    public String buildMessage(VolunteerDTO volunteer) {
        try {
            return tryBuildMessage(volunteer);
        } catch (Exception e) {
            return "";
        }
    }

    private String tryBuildMessage(VolunteerDTO volunteer) {

        List<VolunteerSessionDTO> assignments = volunteerService.getSessions(volunteer.getId());
        Context context = new Context();
        ConfirmationDTO dto = new ConfirmationDTO();

        String name = assignments.get(0).getForename()+" "+assignments.get(0).getSurname();

        dto.setVolunteerName(name);
        dto.setAssignments(assignments);

        context.setVariable("volunteer", dto);
        return templateEngine.process("emailTemplate", context);
    }



}