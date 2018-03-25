package org.camra.staffing.ui.volunteer.forms;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.ApplicationFormUI;
import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.*;
import org.camra.staffing.data.repository.FormAreaRepository;
import org.camra.staffing.data.repository.SessionRepository;
import org.camra.staffing.data.service.AssignedCountsService;
import org.camra.staffing.data.service.SessionService;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.email.EmailMessage;
import org.camra.staffing.email.EmailSender;
import org.camra.staffing.ui.admin.grids.Columns;
import org.camra.staffing.util.CamraMember;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@SpringComponent
@UIScope
public class ApplicationFormLogic extends ApplicationForm {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE dd MMM");
    private static final String[] colours = {"#33af33","#5daf35","#88b038","#b8ba35","#e7c333","#dfa533","#d68533","#ae5c33","#853333"};
    private static final List<Preference> options = Arrays.asList(Preference.values());

    @Value("${email.welcome.subject}") private String emailSubject;
    @Value("${mainform.verified}") private String verified;
    @Value("${mainform.retrieved}") private String retrieved;
    @Value("${mainform.submitted.title}") private String submittedTitle;
    @Value("${mainform.submiited.message}") private String submittedMessage;
    @Value("${email.submitted.verified}") private String emailMessageVerified;
    @Value("${email.submitted.notverified}") private String emailMessageNotVerified;
    @Value("${email.nosessions}") private String emailNoSessions;


    @Autowired private FormAreaRepository formAreaRepository;
    @Autowired private SessionService sessionService;
    @Autowired private AssignedCountsService assignedCountsService;
    @Autowired private VolunteerService volunteerService;
    @Autowired private ApplicationFormUI formUI;
    @Autowired private EmailSender emailSender;
    private Map<Integer,ComboBox<Preference>> areaSelectors= new HashMap<>();
    private Map<LocalDate, List<SessionDTO>> sessionMap;
    private Map<Integer,CheckBox> sessionSelectors = new HashMap<>();
    private Map<Integer, List<AssignedCountsDTO>> counts;
    private Binder<VolunteerDTO> binder = new Binder<>(VolunteerDTO.class);
    private VolunteerDTO volunteer;

    @PostConstruct
    private void init() {

        createAreas();

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
        submit.addClickListener(this::submit);
    }

    private void createAreas() {
        List<FormArea> areaList = formAreaRepository.findAllByOrderById();
        
        ResponsiveLayout responsiveLayout = new ResponsiveLayout().withDefaultRules(8,6,4,4).withFlexible();
        ResponsiveRow row = responsiveLayout.addRow().withGrow(false).withSpacing(true);
        for (FormArea area : areaList) {
            ComboBox<Preference> selector = createAreaSelector(area);
            row.addComponent(selector);
            areaSelectors.put(area.getId(), selector);
        }
        areas.addComponent(responsiveLayout);
    }

    private ComboBox<Preference> createAreaSelector(FormArea area) {
        ComboBox<Preference> selector = new ComboBox<>(area.getName(), options);
        selector.setEmptySelectionAllowed(false);
        selector.setValue(Preference.DontMind);
        selector.setItemCaptionGenerator(Preference::getCaption);
        return selector;
    }


    private void submit(Button.ClickEvent clickEvent) {
        if (validate()) {

            binder.writeBeanIfValid(volunteer);
            if (membership.isVisible()) {
                volunteer.setMembership(membership.getValue());
            } else {
                volunteer.setMembership(enterMembership.getValue());
            }
            for (int areaId : areaSelectors.keySet()) {
               ComboBox<Preference> preferenceComboBox = areaSelectors.get(areaId);
               Preference preference = preferenceComboBox.getValue();
               volunteer.addArea(areaId, preference);
            }

            for (int sessionId : sessionSelectors.keySet()) {
                CheckBox sessionCheckBox = sessionSelectors.get(sessionId);
                if (sessionCheckBox.getValue()) {
                    volunteer.addSession(sessionId);
                } else {
                    volunteer.removeSession(sessionId);
                }
            }

            volunteerService.saveVolunteer(volunteer);

            sendEmail(volunteer);
            formUI.showMessage(submittedTitle, submittedMessage);
        }
    }

    private void bindFields() {
        binder.forField(forename).bind("forename");
        binder.forField(surname).bind("surname");
        binder.forField(email).bind("email");
        binder.forField(enterMembership).bind("membership");
        binder.forField(membership).bind("membership");
        binder.forField(manager).bind("managervouch");
        binder.forField(sia).bind("sia");
        binder.forField(firstaid).bind("firstaid");
        binder.forField(forklift).bind("forklift");
        binder.forField(cellar).bind("cellar");
        binder.forField(comment).bind("comment");
        binder.forField(instructions).bind("instructions");
    }

    public void setVolunteer(VolunteerDTO volunteer) {
        this.volunteer = volunteer;
        this.volunteer.setEmailVerified(true);
        binder.readBean(this.volunteer);
        forename.setReadOnly(true);
        surname.setReadOnly(true);
        email.setReadOnly(true);

        if (volunteer.isRetrieved()) {
            success.setVisible(true);
            success.setValue(retrieved);
        } else if (volunteer.isVerified()) {
            success.setVisible(true);
            success.setValue(verified);
        }

        if (volunteer.isVerified()) {
            membership.setReadOnly(true);
            membership.setVisible(true);
            membershipLabel.setVisible(false);
            membershipFields.setVisible(false);
        }

        for (ComboBox<Preference> areaSelector : areaSelectors.values()) {
            Preference defaultPreference = volunteer.isRetrieved() ? Preference.No : Preference.DontMind;
            areaSelector.setValue(defaultPreference);
        }

        for (int areaId : volunteer.getAreas().keySet()) {
            ComboBox<Preference> selector = areaSelectors.get(areaId);
            if (selector!=null) {
                selector.setValue(volunteer.getAreas().get(areaId).getPreference());
            }
        }
        for (int sessionId : volunteer.getSessions()) {
            sessionSelectors.get(sessionId).setValue(true);
        }
    }

    private void doSessions() {
        counts = assignedCountsService.getCountsBySession();
        sessionMap = sessionService.getSessionMap();
        sessionMap.keySet().forEach(this::createDay);
    }

    private void createDay(LocalDate localDate) {
        VerticalLayout day = new VerticalLayout();
        day.setSpacing(false);
        day.setMargin(false);
        ResponsiveLayout dayLaneHolder = new ResponsiveLayout().withDefaultRules(8,6,4,2).withFlexible();
        ResponsiveRow dayLane = dayLaneHolder.addRow().withGrow(false).withSpacing(false);
        dayLane.setWidth("100%");
        Label dayLabel = new Label(DAY_FORMAT.format(localDate));
        dayLabel.addStyleName(ValoTheme.LABEL_H2);
        day.addComponent(dayLabel);
        day.addComponent(dayLane);
        sessionMap.get(localDate).forEach(session -> dayLane.addComponent(createSession(session)));
        sessions.addComponent(day);
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

    private boolean validate() {
        boolean membershipOk = enterMembership.getValue()!=null && enterMembership.getValue().matches("\\d+");
        boolean managerOK = StringUtils.hasText(manager.getValue());
        List<String> errorMessages = new ArrayList<>();

        if (!membershipOk && !managerOK) {
            errorMessages.add("Please either enter your CAMRA Membership Number, or the name of a member of the Organising Team who knows you");
        }

        if (!instructions.getValue()) {
            errorMessages.add("Please indicate that you have read and understood the instructions for staff");
        }

        boolean areasOK = false;
        for (ComboBox<Preference> areaSelector : areaSelectors.values()) {
            areasOK |= areaSelector.getValue()!=Preference.No;
        }
        if (!areasOK) {
            errorMessages.add("Please do not say No to all Areas!");
        }

        error.setVisible(!errorMessages.isEmpty());
        error.setValue(errorMessages.stream().collect(Collectors.joining("<br/>")));
        return errorMessages.isEmpty();
    }

    private String getIcon(int assigned, int required) {
        double ratio = (double)assigned / (double)required;
        ratio = Math.pow(ratio, 2);
        int r = (int)(ratio*8);
        r = r>8 ? 8 : r;
        return Columns.getIconCode(colours[r], VaadinIcons.CIRCLE);
    }

    private void sendEmail(VolunteerDTO volunteer) {
        String messageText = volunteer.isVerified() ? emailMessageVerified : emailMessageNotVerified;
        messageText = messageText.replace("FORENAME", volunteer.getForename())
                .replace("SURNAME", volunteer.getSurname())
                .replace("SESSIONS", getSessions(volunteer))
                .replace("YESAREAS", getYesAreas(volunteer))
                .replace("DONTMINDAREAS", getDontMindAreas(volunteer))
                .replace("UUID", volunteer.getUuid()==null?"":volunteer.getUuid());

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setSubject(emailSubject);
        emailMessage.setBody(messageText);
        emailMessage.addRecipient(volunteer.getEmail());
        emailSender.sendMessage(emailMessage);
    }

    private String getSessions(VolunteerDTO volunteer) {
        if (volunteer.getSessions().isEmpty()) {
            return emailNoSessions;
        } else {
            List<SessionDTO> sessions = sessionService.getSessions();
            String sessionList = sessions.stream().filter(session -> volunteer.getSessions().contains(session.getId()))
                    .map(SessionDTO::getLongDescription)
                    .collect(Collectors.joining("</li><li>"));
            return "<ul><li>" + sessionList + "</li></ul>";
        }
    }

    private String getYesAreas(VolunteerDTO volunteer) {
        List<FormArea> formAreas = formAreaRepository.findAll();
        String areaList = formAreas.stream().filter(area -> (volunteer.getAreas().get(area.getId()).getPreference()==Preference.Yes))
                .map(FormArea::getName)
                .collect(Collectors.joining("</li><li>"));
        return "<ul><li>" + areaList + "</li></ul>";
    }

    private String getDontMindAreas(VolunteerDTO volunteer) {
        List<FormArea> formAreas = formAreaRepository.findAll();
        String areaList = formAreas.stream().filter(area -> (volunteer.getAreas().get(area.getId()).getPreference()==Preference.DontMind))
                .map(FormArea::getName)
                .collect(Collectors.joining("</li><li>"));
        return "<ul><li>" + areaList + "</li></ul>";
    }


}
