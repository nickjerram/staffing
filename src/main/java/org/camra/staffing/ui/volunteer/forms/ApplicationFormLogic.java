package org.camra.staffing.ui.volunteer.forms;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.entity.FormArea;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.repository.FormAreaRepository;
import org.camra.staffing.data.repository.SessionRepository;
import org.camra.staffing.data.service.SessionService;
import org.camra.staffing.ui.admin.grids.Columns;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@SpringComponent
@UIScope
public class ApplicationFormLogic extends ApplicationForm {

    @Autowired private FormAreaRepository formAreaRepository;
    @Autowired private SessionService sessionService;
    private Map<Integer,ComboBox<Preference>> areaSelectors= new HashMap<>();
    private Map<LocalDate, List<SessionDTO>> sessionMap;

    @PostConstruct
    private void init() {
        List<FormArea> areaList = formAreaRepository.findAll();
        List<Preference> options = Arrays.asList(Preference.values());
        for (FormArea area : areaList) {
            ComboBox<Preference> selector = new ComboBox<>(area.getName(), options);
            selector.setEmptySelectionAllowed(false);
            selector.setValue(Preference.DontMind);
            selector.setItemCaptionGenerator(Preference::getCaption);
            areaSelectors.put(area.getId(), selector);
            areas.addComponent(selector);
        }
        level0.setValue(Columns.getIconCode("#33af33", VaadinIcons.CIRCLE));
        level1.setValue(Columns.getIconCode("#5daf35", VaadinIcons.CIRCLE));
        level2.setValue(Columns.getIconCode("#88b038", VaadinIcons.CIRCLE));
        level3.setValue(Columns.getIconCode("#b8ba35", VaadinIcons.CIRCLE));
        level4.setValue(Columns.getIconCode("#e7c333", VaadinIcons.CIRCLE));
        level5.setValue(Columns.getIconCode("#dfa533", VaadinIcons.CIRCLE));
        level6.setValue(Columns.getIconCode("#d68533", VaadinIcons.CIRCLE));
        level7.setValue(Columns.getIconCode("#ae5c33", VaadinIcons.CIRCLE));
        level8.setValue(Columns.getIconCode("#853333", VaadinIcons.CIRCLE));
        doSessions();
    }

    public void setMember(CamraMember member) {
        setValue(forename, member.getForename());
        setValue(surname, member.getSurname());
        setValue(membership, member.getNumber());
        setValue(email, member.getEmail());
    }

    private void setValue(TextField field, String value) {
        field.setValue(value);
        field.setReadOnly(true);
    }

    private void doSessions() {
        sessionMap = sessionService.getSessionMap();
        sessionMap.keySet().forEach(this::createDay);
    }

    private void createDay(LocalDate localDate) {
        HorizontalLayout dayLane = new HorizontalLayout();
        dayLane.addComponent(new Label("Day "+localDate));
        sessionMap.get(localDate).forEach(session -> dayLane.addComponent(new Label(session.getStartTime()+"--"+ session.getFinishTime())));
        sessions.addComponent(dayLane);
    }


}
