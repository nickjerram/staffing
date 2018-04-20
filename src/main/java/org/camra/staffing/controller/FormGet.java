package org.camra.staffing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.service.AssignedCountsService;
import org.camra.staffing.data.service.FormService;
import org.camra.staffing.data.service.SessionService;
import org.camra.staffing.data.service.VolunteerService;
import org.eclipse.persistence.sessions.serializers.JSONSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Controller
@SessionScope
public class FormGet {

    public static final String VOLUNTEER = "volunteer";
    @Autowired private HttpSession httpSession;
    @Autowired private VolunteerService volunteerService;
    @Autowired private FormManager formManager;


    /**
     * Request Member Verification Form
     */
    @GetMapping("/")
    public String getMemberVerificationForm() {
        formManager.setVerify();
        return "form";
    }

    /**
     * Request Email Verification Form
     */
    @GetMapping("/email")
    public String getEmailVerificationForm() {
        formManager.setEmail();
        return "form";
    }

    /**
     * Retrieve a form via UUID
     * @param uuid
     */
    @GetMapping("/form/{uuid}")
    public String get(@PathVariable String uuid) {
        Optional<VolunteerDTO> v = volunteerService.getVolunteer(uuid);
        if (v.isPresent()) {
            formManager.setVolunteerFound();
            formManager.setVolunteer(v.get());
        } else {
            formManager.setVolunteerNotFound();
        }
        return "form";
    }


}
