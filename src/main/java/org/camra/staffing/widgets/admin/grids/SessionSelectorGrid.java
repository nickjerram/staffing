package org.camra.staffing.widgets.admin.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class SessionSelectorGrid extends Grid<SessionSelectorDTO> {

    @Autowired private VolunteerService volunteerService;
    private DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private Set<SessionSelectorDTO> newSessions = new HashSet<>();
    private Set<SessionSelectorDTO> existingSessions;
    private Consumer<Void> changeHandler;

    @PostConstruct
    private void init() {
        addColumn(this::formatSessionName).setCaption("Session").setId("session");
        addColumn(this::formatSessionSelected, new HtmlRenderer()).setCaption("Start");
        addItemClickListener(this::sessionClick);

        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
    }

    public void setVolunteerId(int volunteerId) {
        List<SessionSelectorDTO> sessions = volunteerService.getPossibleSessions(volunteerId);
        existingSessions = sessions.stream().filter(s->s.isSelected()).collect(Collectors.toSet());
        setItems(sessions);
    }

    public void setChangeHandler(Consumer<Void> handler) {
        this.changeHandler = handler;
    }

    public List<SessionSelectorDTO> getNewSessions() {
        return new ArrayList<>(newSessions);
    }

    private void sessionClick(ItemClick<SessionSelectorDTO> sessionClick) {
        SessionSelectorDTO session = sessionClick.getItem();
        if (!existingSessions.contains(session)) {
            if (newSessions.contains(session)) {
                session.setSelected(false);
                newSessions.remove(session);
            } else {
                session.setSelected(true);
                newSessions.add(session);
            }
            getDataProvider().refreshAll();
            if (changeHandler!=null) {
                changeHandler.accept(null);
            }
        }
    }

    private String formatSessionName(SessionSelectorDTO session) {
        return session.getSessionName()+" ("+session.getStartTime().format(HHMM)+"-"+session.getFinishTime().format(HHMM)+")";
    }

    private String formatSessionSelected(SessionSelectorDTO session) {
        return session.isSelected() ?  Columns.getIconCode("#0f0", VaadinIcons.CHECK_SQUARE) : "";
    }


}
