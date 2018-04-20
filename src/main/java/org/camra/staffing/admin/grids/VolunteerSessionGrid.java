package org.camra.staffing.admin.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.VolunteerSession;
import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.camra.staffing.data.provider.VolunteerSessionDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.Date;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class VolunteerSessionGrid extends AbstractGrid<VolunteerSessionDTO, VolunteerSessionView> {

    @Autowired private VolunteerSessionDataProvider provider;
    @Autowired private Manager manager;
    private boolean respectLock = true;

    @PostConstruct
    private void init() {
        setDataProvider(provider);
        setSizeFull();
        addItemClickListener(this::volunteerSessionClick);
    }

    public void setVolunteerId(int volunteerId) {
        respectLock = false;
        provider.setVolunteerId(volunteerId);
        if (manager.isSuperUser()) {
            addColumn(this::formatEditForVolunteer, new HtmlRenderer()).setWidth(50).setId("edit");
        }
        addColumn(VolunteerSessionDTO::getSessionName).setWidth(200).setCaption("Session");
        addStandardColumns();
        provider.refreshAll();
    }

    public void setSessionId(int sessionId) {
        respectLock = true;
        provider.setSessionId(sessionId);
        if (manager.isSuperUser()) {
            addColumn(this::formatEditForSession, new HtmlRenderer()).setWidth(50).setId("edit");
        }
        addColumn(this::formatName).setCaption("Volunteer").setWidth(200);
        addStandardColumns();
        provider.refreshAll();
    }

    private void volunteerSessionClick(ItemClick<VolunteerSessionDTO> event) {
        if (event.getColumn().getId()==null) return;
        boolean locked = respectLock && event.getItem().isLocked();
        if (!locked && event.getColumn().getId().equals("edit")) {
            if (editHandler!=null) {
                editHandler.accept(event.getItem());
            }
        }
    }

    private void addStandardColumns() {
        addColumn(VolunteerSessionDTO::getAreaName).setCaption("Area").setId("areaName").setWidth(200);
        addColumn(this::formatStartTime).setCaption("Start").setWidth(100);
        addColumn(this::formatFinishTime).setCaption("Finish").setWidth(100);
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Staff").setWidth(100);
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked").setWidth(100);
        addColumn(VolunteerSessionDTO::getTokens).setCaption("Tokens").setWidth(100);
        addColumn(VolunteerSessionDTO::getComment).setCaption("Comment").setExpandRatio(1);

        addStringFilters("areaName");
    }

    protected SortableDataProvider<VolunteerSessionDTO,VolunteerSessionView> dataProvider() {
        return provider;
    }

    private String formatEditForVolunteer(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLocked() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        return Columns.getIconCode("#0c0", icon);
    }

    protected String formatEditForSession(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLocked() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        String colour = assignment.isLocked() ? "#c00" : "#0c0";
        return Columns.getIconCode(colour, icon);
    }

    private String formatName(VolunteerSessionDTO assignment) {
        return assignment.getForename()+" "+assignment.getSurname();
    }

    private String formatStartTime(VolunteerSessionDTO assignment) {
        return assignment.getStart()==null ? "" : formatTime(assignment.getStart());
    }

    private String formatFinishTime(VolunteerSessionDTO assignment) {
        return assignment.getFinish()==null ? "" : formatTime(assignment.getFinish());
    }

    private String formatAssigned(VolunteerSessionDTO assignment) {
        return Columns.formatRatio(assignment.getAssigned(), assignment.getRequired());
    }

    private String formatTime(Date time) {
        return VolunteerSessionDTO.TIME.format(time);
    }

    private String formatWorked(VolunteerSessionDTO assignment) {
        return Columns.formatBoolean("#00aa00", VaadinIcons.CIRCLE, assignment.isWorked());
    }


}
