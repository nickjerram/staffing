package org.camra.staffing.ui.volunteer.forms;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.*;
import org.camra.staffing.data.repository.FormAreaRepository;
import org.camra.staffing.data.repository.SessionRepository;
import org.camra.staffing.data.service.AssignedCountsService;
import org.camra.staffing.data.service.SessionService;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.admin.grids.Columns;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@SpringComponent
@UIScope
public class ApplicationFormLogic extends ApplicationForm {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE dd MMM");
    private static final String[] colours = {"#33af33","#5daf35","#88b038","#b8ba35","#e7c333","#dfa533","#d68533","#ae5c33","#853333"};

    @Autowired private FormAreaRepository formAreaRepository;
    @Autowired private SessionService sessionService;
    @Autowired private AssignedCountsService assignedCountsService;
    @Autowired private VolunteerService volunteerService;
    private Map<Integer,ComboBox<Preference>> areaSelectors= new HashMap<>();
    private Map<LocalDate, List<SessionDTO>> sessionMap;
    private Map<Integer,CheckBox> sessionSelectors = new HashMap<>();
    private Map<Integer, List<AssignedCountsDTO>> counts;
    BeanValidationBinder<VolunteerDTO> binder = new BeanValidationBinder<>(VolunteerDTO.class);

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
        bindFields();
    }

    private void bindFields() {
        binder.forField(forename).bind("forename");
        binder.forField(surname).bind("surname");
        binder.forField(email).bind("email");
        binder.forField(enterMembership).bind("membership");
        binder.forField(manager).bind("managervouch");
    }

    public boolean setUUID(String uuid) {
        Optional<VolunteerDTO> volunteerDTO = volunteerService.getVolunteer(uuid);
        volunteerDTO.ifPresent(volunteer -> binder.readBean(volunteer));
        return volunteerDTO.isPresent();
    }

    public void setMember(CamraMember member) {
        membershipLabel.setVisible(false);
        membershipFields.setVisible(false);
        membership.setVisible(true);
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
        counts = assignedCountsService.getCountsBySession();
        sessionMap = sessionService.getSessionMap();
        sessionMap.keySet().forEach(this::createDay);
    }

    private void createDay(LocalDate localDate) {
        CssLayout dayLane = new CssLayout();
        dayLane.setWidth("100%");
        Label dayLabel = new Label(DAY_FORMAT.format(localDate));
        dayLabel.setWidth("20%");
        dayLane.addComponent(dayLabel);
        sessionMap.get(localDate).forEach(session -> dayLane.addComponent(createSession(session)));
        sessions.addComponent(dayLane);
    }

    private HorizontalLayout createSession(SessionDTO session) {
        List<AssignedCountsDTO> countForSession = counts.get(session.getId());
        int totalAssigned = countForSession.stream().mapToInt(AssignedCountsDTO::getAssigned).sum();
        int totalRequired = countForSession.stream().mapToInt(AssignedCountsDTO::getRequired).sum();

        String icon = getIcon(totalAssigned, totalRequired);
        Label label = new Label(session.getDescription()+icon);
        label.setContentMode(ContentMode.HTML);
        HorizontalLayout layout = new HorizontalLayout();
        CheckBox box = new CheckBox();
        sessionSelectors.put(session.getId(), box);
        layout.addComponents(label, box);
        return layout;
    }

    private String getIcon(int assigned, int required) {
        double ratio = (double)assigned / (double)required;
        ratio = Math.pow(ratio, 2);
        int r = (int)(ratio*8);
        r = r>8 ? 8 : r;
        return Columns.getIconCode(colours[r], VaadinIcons.CIRCLE);
    }


}
